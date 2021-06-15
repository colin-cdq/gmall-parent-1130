package com.cdq.uesr.service.impl;

import com.alibaba.fastjson.JSON;
import com.cdq.feign.ProductFeignClient11111;
import com.cdq.model.product.BaseCategoryView;
import com.cdq.model.product.SkuImage;
import com.cdq.model.product.SkuInfo;
import com.cdq.model.product.SpuSaleAttr;
import com.cdq.search.client.SearchFeignClient;
import com.cdq.uesr.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-24 21:17
 **/
@Service
public class ItemServiceImpl  implements ItemService {
    //注入service-product-client的bean
    @Autowired
    ProductFeignClient11111 productFeignClient;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    SearchFeignClient searchFeignClient;

    @Override
    public Map<String, Object> item(Long skuId) {
        //时间代码
        long start = System.currentTimeMillis();

        Map<String, Object> map = new HashMap<> ();


        // 需要调用service-product模块查询基础数据
        CompletableFuture<SkuInfo> skuCompletableFuture = CompletableFuture.supplyAsync ( new Supplier<SkuInfo> () {
            @Override
            public SkuInfo get() {
                SkuInfo skuInfo = productFeignClient.getSkuById(skuId);
                return skuInfo;
            }
        },threadPoolExecutor);


        // sku图片
        CompletableFuture<Void> imgCompletableFuture = skuCompletableFuture.thenAcceptAsync(new Consumer<SkuInfo> () {
            @Override
            public void accept(SkuInfo skuInfo) {
                List<SkuImage> skuImages = productFeignClient.getSkuImagesBySkuId(skuId);
                skuInfo.setSkuImageList(skuImages);
                map.put("skuInfo",skuInfo);
            }
        },threadPoolExecutor);

        // sku价格
        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                BigDecimal price = productFeignClient.getSkuPriceById(skuId);
                map.put("price",price);
            }
        },threadPoolExecutor);

        // 商品分类
        CompletableFuture<Void> categoryCompletableFuture = skuCompletableFuture.thenAcceptAsync(new Consumer<SkuInfo> () {
            @Override
            public void accept(SkuInfo skuInfo) {
                BaseCategoryView categoryView = productFeignClient.getCategoryViewByC3Id(skuInfo.getCategory3Id());
                map.put("categoryView",categoryView);
            }
        },threadPoolExecutor);

        // 销售属性列表
        CompletableFuture<Void> spuSaleAttrCompletableFuture = skuCompletableFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                List<SpuSaleAttr> spuSaleAttrs = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getSpuId(), skuInfo.getId());
                map.put("spuSaleAttrList", spuSaleAttrs);
            }
        },threadPoolExecutor);

        // 销售属性对应skuhash表格
        CompletableFuture<Void> valuesCompletableFuture = skuCompletableFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                List<Map<String,Object>> valuesSkus = productFeignClient.getValuesSku(skuInfo.getSpuId());// dao层返回的结果是一个集合

                if(null!=valuesSkus&&valuesSkus.size()>0){
                    Map<String,Object> valuesSkusMap = new HashMap<>();
                    for (Map<String, Object> valuesSku : valuesSkus) {
                        String valueIds = (String)valuesSku.get("valueIds");
                        Integer skuIdForValues = (Integer)valuesSku.get("sku_id");
                        valuesSkusMap.put(valueIds,skuIdForValues);
                    }
                    String valuesSkuJson = JSON.toJSONString(valuesSkusMap);
                    map.put("valuesSkuJson",valuesSkuJson);
                }
            }
        },threadPoolExecutor);
        CompletableFuture.allOf(skuCompletableFuture,imgCompletableFuture,priceCompletableFuture,categoryCompletableFuture,spuSaleAttrCompletableFuture,valuesCompletableFuture).join();

        long end = System.currentTimeMillis();
        System.out.println("item服务执行时间："+(end-start));

        // 调用search搜索服务更新该商品的热度值
        searchFeignClient.hotScore(skuId);

        return map;
    }
}

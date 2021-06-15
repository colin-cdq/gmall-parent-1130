package com.cdq.feign;

import com.cdq.model.list.Goods;
import com.cdq.model.product.BaseCategoryView;
import com.cdq.model.product.SkuImage;
import com.cdq.model.product.SkuInfo;
import com.cdq.model.product.SpuSaleAttr;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-25 20:43
 **/
@FeignClient(value = "service-product")
public interface ProductFeignClient11111 {


    @RequestMapping("api/product/getSkuById/{skuId}")
    SkuInfo getSkuById(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getSkuImagesBySkuId/{skuId}")
    List<SkuImage> getSkuImagesBySkuId(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getSkuPriceById/{skuId}")
    BigDecimal getSkuPriceById(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getCategoryViewByC3Id/{category3Id}")
    BaseCategoryView getCategoryViewByC3Id(@PathVariable("category3Id") Long category3Id);

    @RequestMapping("api/product/getSpuSaleAttrListCheckBySku/{spuId}/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("spuId") Long spuId , @PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getValuesSku/{spuId}")
    List<Map<String, Object>> getValuesSku(@PathVariable Long spuId);

    @RequestMapping("api/product/getCategoryView")
    List<BaseCategoryView> getCategoryView();

    @RequestMapping("api/product/getGoodsBySkuId/{skuId}")
    Goods getGoodsBySkuId(@PathVariable("skuId") Long skuId);
}

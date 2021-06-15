package com.cdq.product.controller;

import com.cdq.model.list.Goods;
import com.cdq.model.product.BaseCategoryView;
import com.cdq.model.product.SkuImage;
import com.cdq.model.product.SkuInfo;
import com.cdq.model.product.SpuSaleAttr;
import com.cdq.product.service.BaseCategory3Service;
import com.cdq.product.service.CategoryService;
import com.cdq.product.service.SkuService;
import com.cdq.product.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-24 14:17
 **/
@RestController
@RequestMapping("api/product/")
@CrossOrigin
public class ProductApiController {
    @Autowired
    SkuService skuService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    SpuService spuService;

//    @Autowired
//    BaseCategory1Controller baseCategory1Controller;
//
//    @Autowired
//    BaseCategory2Controller baseCategory2Controller;

    @Autowired
    BaseCategory3Service baseCategory3Service;


    @RequestMapping("getSpuSaleAttrListCheckBySku/{spuId}/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("spuId") Long spuId, @PathVariable("skuId")  Long skuId){
        List<SpuSaleAttr> spuSaleAttrs = spuService.getSpuSaleAttrListCheckBySku(spuId,skuId);
        return spuSaleAttrs;
    }


    @RequestMapping("getCategoryViewByC3Id/{category3Id}")
    BaseCategoryView getCategoryViewByC3Id(@PathVariable("category3Id") Long category3Id){
        BaseCategoryView baseCategoryView = baseCategory3Service.getCategoryViewByC3Id(category3Id);

        return baseCategoryView;
    }

    @RequestMapping("getSkuPriceById/{skuId}")
    BigDecimal getSkuPriceById(@PathVariable("skuId") Long skuId){
        BigDecimal price = skuService.getSkuPriceById(skuId);

        return price;
    }

    @RequestMapping("getSkuImagesBySkuId/{skuId}")
    List<SkuImage> getSkuImagesBySkuId(@PathVariable("skuId") Long skuId){

        List<SkuImage> skuImages = skuService.getSkuImagesBySkuId(skuId);

        return skuImages;
    }

    @RequestMapping("getSkuById/{skuId}")
    SkuInfo getSkuById(@PathVariable("skuId") Long skuId){
        SkuInfo skuInfo = skuService.getSkuById(skuId);

        return skuInfo;
    }

    @RequestMapping("getValuesSku/{spuId}")
    List<Map<String,Object>> getValuesSku(@PathVariable("spuId") Long spuId){
        List<Map<String,Object>> maps = skuService.getValuesSku(spuId);
        return maps;
    }

    @RequestMapping("getCategoryView")
    List<BaseCategoryView> getCategoryView(){
        List<BaseCategoryView> categoryViews = categoryService.getCategoryView();
        return  categoryViews;
    }

    @RequestMapping("getGoodsBySkuId/{skuId}")
    Goods getGoodsBySkuId(@PathVariable("skuId") Long skuId){

        Goods goods = skuService.getGoodsBySkuId(skuId);
        return goods;
    }
}

package com.cdq.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cdq.model.product.BaseSaleAttr;
import com.cdq.model.product.SpuImage;
import com.cdq.model.product.SpuInfo;
import com.cdq.model.product.SpuSaleAttr;

import java.util.List;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-19 00:08
 **/
public interface SpuService {
    IPage<SpuInfo> spuList(Long page, Long limit, Long category3Id);

    List<BaseSaleAttr> baseSaleAttrList();

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuImage> spuImageList(Long spuId);

    List<SpuSaleAttr> spuSaleAttrList(Long spuId);


    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long spuId , Long skuId);
}

package com.cdq.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cdq.model.list.Goods;
import com.cdq.model.product.SkuImage;
import com.cdq.model.product.SkuInfo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-20 15:20
 **/

public interface SkuService {
    void saveSkuInfo(SkuInfo skuInfo);


    IPage<SkuInfo> list(Long page , long limit);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

    BigDecimal getSkuPriceById(Long skuId);

    List<SkuImage> getSkuImagesBySkuId(Long skuId);

    SkuInfo getSkuById(Long skuId);

    List<Map<String, Object>> getValuesSku(Long spuId);

    Goods getGoodsBySkuId(Long skuId);
}

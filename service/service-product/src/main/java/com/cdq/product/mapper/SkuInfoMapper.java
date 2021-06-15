package com.cdq.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdq.model.list.Goods;
import com.cdq.model.product.SkuInfo;
import org.apache.ibatis.annotations.Param;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-20 15:22
 **/
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {
    Goods getGoodsBySkuId(@Param ( "skuId" ) Long skuId);
}

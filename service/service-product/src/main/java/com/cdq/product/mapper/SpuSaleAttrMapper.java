package com.cdq.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdq.model.product.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-19 00:11
 **/
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(@Param ( "spuId" ) Long spuId ,@Param ( "skuId" ) Long skuId);
}

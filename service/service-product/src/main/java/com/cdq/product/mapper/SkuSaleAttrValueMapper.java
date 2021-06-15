package com.cdq.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdq.model.product.SkuSaleAttrValue;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-20 15:24
 **/
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    List<Map<String, Object>> selectValuesSku(@Param ( "spuId" ) Long spuId);
}

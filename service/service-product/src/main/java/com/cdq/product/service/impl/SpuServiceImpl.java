package com.cdq.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdq.aop.GmallCache;
import com.cdq.model.product.*;
import com.cdq.product.mapper.*;
import com.cdq.product.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-19 00:09
 **/
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Override
    public IPage<SpuInfo> spuList(Long page, Long limit, Long category3Id) {

        IPage<SpuInfo> infoIPage = new Page<SpuInfo> (page,limit);

        QueryWrapper<SpuInfo> queryWrapper = new QueryWrapper<> ();

        queryWrapper.eq("category3_id",category3Id);

        IPage<SpuInfo> infoIPageResult = spuInfoMapper.selectPage(infoIPage, queryWrapper);

        return infoIPageResult;
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {

        // 保存spuInfo
        spuInfoMapper.insert(spuInfo);

        // 生成主键spuId
        Long spuId = spuInfo.getId();


        // 根据主键保存图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(null!=spuImageList&&spuImageList.size()>0){
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuId);
                spuImageMapper.insert(spuImage);
            }
        }

        // 根据主键保存spu销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();

        if(null!=spuSaleAttrList&&spuSaleAttrList.size()>0){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuId);
                //前端传过来的
                // spuSaleAttr.setBaseSaleAttrId();
                spuSaleAttrMapper.insert(spuSaleAttr);
                // 根据主键保存spu销售属性值
                for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttr.getSpuSaleAttrValueList()) {
                    spuSaleAttrValue.setSpuId(spuId);
                    spuSaleAttrValue.setBaseSaleAttrId(spuSaleAttr.getBaseSaleAttrId());// 插入用来唯一的联合主键销售属性id
                    spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());//插入销售属性表的销售属性名称
                    spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                }
            }
        }
    }

    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);
        List<SpuImage> spuImages = spuImageMapper.selectList(queryWrapper);

        return spuImages;
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {
        QueryWrapper<SpuSaleAttr> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectList(queryWrapper);

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrs) {

            QueryWrapper<SpuSaleAttrValue> queryWrapperValue = new QueryWrapper<>();
            queryWrapperValue.eq("spu_id",spuId);
            queryWrapperValue.eq("base_sale_attr_id",spuSaleAttr.getBaseSaleAttrId());
            List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueMapper.selectList(queryWrapperValue);
            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);

        }

        return spuSaleAttrs;
    }

    @GmallCache
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long spuId , Long skuId) {
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(spuId,skuId);

        return spuSaleAttrs;
    }


}

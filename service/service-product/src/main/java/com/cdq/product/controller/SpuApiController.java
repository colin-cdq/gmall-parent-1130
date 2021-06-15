package com.cdq.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cdq.common.util.Result;
import com.cdq.model.product.BaseSaleAttr;
import com.cdq.model.product.SpuImage;
import com.cdq.model.product.SpuInfo;
import com.cdq.model.product.SpuSaleAttr;
import com.cdq.product.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-19 00:07
 **/
@RestController
@RequestMapping("admin/product/")
@CrossOrigin
public class SpuApiController {

    @Autowired
    SpuService spuService;

    /*
    * 添加SPU的保存
    * */

    @RequestMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){

        spuService.saveSpuInfo(spuInfo);

        return Result.fail();
    }

    /*
    *销售属性
    * */

    @RequestMapping("baseSaleAttrList")
    public Result baseSaleAttrList(){

        List<BaseSaleAttr> baseSaleAttrs =  spuService.baseSaleAttrList();

        return Result.ok(baseSaleAttrs);
    }

    /*
    * spu分页列表
    * */

    @RequestMapping("{page}/{limit}")
    public Result spuList(@PathVariable("page") Long page, @PathVariable("limit") Long limit, Long category3Id){

        IPage<SpuInfo> infoIPage =  spuService.spuList(page,limit,category3Id);

        return Result.ok(infoIPage);
    }

    /*
    * 添加SKU加载图片
    * */

    @RequestMapping("spuImageList/{spuId}")
    public Result spuImageList(@PathVariable("spuId") Long spuId){

        List<SpuImage> spuImages = spuService.spuImageList(spuId);

        return Result.ok(spuImages);
    }


    /*
     * 添加SKU获取销售属性
     * */

    @RequestMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable("spuId") Long spuId){

        List<SpuSaleAttr> spuSaleAttrs = spuService.spuSaleAttrList(spuId);

        return Result.ok(spuSaleAttrs);
    }


}

package com.cdq.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cdq.common.util.Result;
import com.cdq.common.util.ResultCodeEnum;
import com.cdq.model.product.SkuInfo;
import com.cdq.product.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-20 15:18
 **/
@RestController
@RequestMapping("admin/product/")
@CrossOrigin
public class SkuApiController {

    @Autowired
    SkuService skuService;


    /*
    添加
    * */

    @RequestMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){

        skuService.saveSkuInfo(skuInfo);

        return Result.ok();
    }

    /*
    * SKU列表
    * */
    @RequestMapping("list/{page}/{limit}")
    public Result list(@PathVariable("page") Long page ,@PathVariable("limit") long limit ){
       IPage<SkuInfo> infoPage = skuService.list(page,limit);
       return   Result.ok(infoPage);
    }

    /*
    * 上架
    * */

    @RequestMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId){

        skuService.onSale(skuId);

        return Result.ok();
    }

    /*
    * 下架
    * */

    @RequestMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId){

        skuService.cancelSale(skuId);

        return Result.ok();
    }
}

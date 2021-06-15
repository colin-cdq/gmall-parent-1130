package com.cdq.activity.controller;

import com.cdq.activity.config.CacheHelper;
import com.cdq.activity.service.SeckillService;
import com.cdq.common.util.MD5;
import com.cdq.common.util.Result;
import com.cdq.common.util.ResultCodeEnum;
import com.cdq.model.activity.OrderRecode;
import com.cdq.model.activity.SeckillGoods;
import com.cdq.model.order.OrderDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @program: ware-manage
 * @description:
 * @author: cdq
 * @create: 2021-06-14 03:11
 **/
@RestController
@RequestMapping("api/activity/seckill")
public class SeckillApiController {
    @Autowired
    SeckillService seckillService;

    @RequestMapping("putSeckillGoods/{skuId}")
    public Result putSeckillGoods(@PathVariable("skuId") Long skuId){

        seckillService.putSeckillGoods(skuId);

        return Result.ok();
    }

    @RequestMapping("getStatus/{skuId}")
    public Result getStatus(@PathVariable("skuId") Long skuId){
        return Result.ok( CacheHelper.get("sku:"+skuId));
    }

    @RequestMapping("findBySkuId/{skuId}")
    SeckillGoods findBySkuId(@PathVariable("skuId") Long skuId){
        return seckillService.findBySkuId(skuId);
    }

    @RequestMapping("findAll")
    List<SeckillGoods> findAll(){
        List<SeckillGoods>  seckillGoods =  seckillService.findAll();

        return seckillGoods;
    }


    @RequestMapping("/auth/getSeckillSkuIdStr/{skuId}")
    Result getSeckillSkuIdStr(@PathVariable("skuId") Long skuId, HttpServletRequest request){
        String userId = request.getHeader("userId");
        String encrypt = MD5.encrypt(userId + skuId);
        return Result.ok(encrypt);
    }

    @RequestMapping("/auth/seckillOrder/{skuId}")
    Result seckillOrder(@PathVariable("skuId") Long skuId, String skuIdStr,HttpServletRequest request){
        String userId = request.getHeader("userId");

        seckillService.seckillOrder(userId,skuId);// 发送抢购消息队列

        return Result.ok();
    }

    @RequestMapping("/auth/checkOrder/{skuId}")
    Result checkOrder(@PathVariable("skuId") Long skuId,HttpServletRequest request){
        String userId = request.getHeader("userId");

        // 下单成功
        String orderId = seckillService.checkOrder(userId);
        if(!StringUtils.isEmpty(orderId)){
            return Result.build(orderId, ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }

        // 抢购成功
        OrderRecode orderRecode = seckillService.checkOrderRecode(userId);
        if(null!=orderRecode){
            return Result.build(null, ResultCodeEnum.SECKILL_SUCCESS);
        }

        // 售罄
        String status = (String)CacheHelper.get("sku:"+skuId);
        if(!StringUtils.isEmpty(status)&&status.equals("0")){
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        }

        // 排队中
        return Result.build(null, ResultCodeEnum.SECKILL_RUN);

    }

    @RequestMapping("getOrderDetailList")
    List<OrderDetail> getOrderDetailList(HttpServletRequest request){

        String userId = request.getHeader("userId");

        return seckillService.getOrderDetailList(userId);
    }


}

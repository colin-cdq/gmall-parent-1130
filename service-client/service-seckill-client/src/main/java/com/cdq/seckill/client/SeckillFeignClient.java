package com.cdq.seckill.client;

import com.cdq.model.activity.SeckillGoods;
import com.cdq.model.order.OrderDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @program: ware-manage
 * @description:
 * @author: cdq
 * @create: 2021-06-14 19:32
 **/
@FeignClient(value = "service-activity")
public interface SeckillFeignClient {
    @RequestMapping("api/activity/seckill/findAll")
    List<SeckillGoods> findAll();

    @RequestMapping("api/activity/seckill/findBySkuId/{skuId}")
    SeckillGoods findBySkuId(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/activity/seckill/getOrderDetailList")
    List<OrderDetail> getOrderDetailList();

}

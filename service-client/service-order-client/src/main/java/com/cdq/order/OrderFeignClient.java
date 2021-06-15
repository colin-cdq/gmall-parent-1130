package com.cdq.order;

import com.cdq.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-06 20:41
 **/
@FeignClient(value = "service-order")
public interface OrderFeignClient {

    @RequestMapping("api/order/getTradeOrder")
    OrderInfo getTradeOrder();

    @RequestMapping("api/order/genTradeNo")
    String genTradeNo();


    @RequestMapping("api/order/getOrderById/{orderId}")
    OrderInfo getOrderById(@PathVariable("orderId") String orderId);
}

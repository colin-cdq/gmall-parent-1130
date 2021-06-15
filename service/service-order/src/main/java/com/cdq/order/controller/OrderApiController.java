package com.cdq.order.controller;

import com.cdq.common.util.Result;
import com.cdq.model.order.OrderInfo;
import com.cdq.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-06 20:50
 **/
@RestController
@RequestMapping("api/order")
public class OrderApiController {
    @Autowired
    OrderService orderService;



//    @RequestMapping("/auth/submitOrder")
//    Result submitOrder(HttpServletRequest request, @RequestBody OrderInfo orderInfo, String tradeNo){
//        String userId = request.getHeader("userId");
//
//        boolean b = orderService.checkTradeNo(userId,tradeNo);
//        if(b){
//            // 保存订单
//            orderInfo.setUserId(Long.parseLong(userId));
//            String orderId = orderService.submitOrder(orderInfo);
//
//            return Result.ok(orderId);
//        }else {
//            return Result.fail();
//        }
//    }

    @RequestMapping("getTradeOrder")
    OrderInfo getTradeOrder(HttpServletRequest request){

        String userId = request.getHeader("userId");

        OrderInfo orderInfo = orderService.getTradeOrder(userId);

        return orderInfo;
    }


    @RequestMapping("genTradeNo")
    String genTradeNo(HttpServletRequest request){
        String userId = request.getHeader("userId");
        return orderService.genTradeNo(userId);
    }


    @RequestMapping("/auth/submitOrder")
    Result submitOrder(HttpServletRequest request, @RequestBody  OrderInfo orderInfo,String tradeNo){
        String userId = request.getHeader("userId");

        //查询交易码是否存在
        boolean b = orderService.checkTradeNo(userId,tradeNo);
        if(b){
            // 保存订单
            orderInfo.setUserId(Long.parseLong(userId));
            String orderId = orderService.submitOrder(orderInfo);

            return Result.ok(orderId);
        }else {
            return Result.fail();
        }

    }

    @RequestMapping("getOrderById/{orderId}")
    OrderInfo getOrderById(@PathVariable("orderId") String orderId){

        OrderInfo orderInfo = orderService.getOrderById(orderId);

        return orderInfo;
    }


}

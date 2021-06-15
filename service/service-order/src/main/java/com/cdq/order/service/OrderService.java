package com.cdq.order.service;

import com.cdq.model.order.OrderInfo;

import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-06 20:52
 **/
public interface OrderService {
    String genTradeNo(String userId);

    OrderInfo getTradeOrder(String userId);

    boolean checkTradeNo(String userId , String tradeNo);

    String submitOrder(OrderInfo orderInfo);

    OrderInfo getOrderById(String orderId);

    void updatePaymentOrder(Map<String, Object> messageMap);
}

package com.cdq.payment.service;

import com.cdq.model.payment.PaymentInfo;

import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-07 03:02
 **/
public interface PaymentService {


    String alipayForm(String userId , String orderId);


    void sendPaymentDelayMessage(String exchange , String routing , Map<String, Object> map);


//    void sendPaymentDelayMessagesx(String outTradeNo);

    void sendPaymentMessage(String exchange , String routing, Map<String, Object> map);

    void updatePayment(PaymentInfo paymentInfo);

    Map<String, Object> checkAliStatus(String out_trade_no);
}

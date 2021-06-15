package com.cdq.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;

import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cdq.model.enums.PaymentStatus;
import com.cdq.model.order.OrderInfo;
import com.cdq.model.payment.PaymentInfo;
import com.cdq.order.OrderFeignClient;
import com.cdq.payment.config.AlipayConfig;
import com.cdq.payment.mapper.PaymentInfoMapper;
import com.cdq.rabbit.raservice.RabbitService;
import com.cdq.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-07 03:06
 **/
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    RabbitService rabbitService;


    @Override
    public String alipayForm(String userId , String orderId) {

        //查询到订单详情
        OrderInfo orderInfo = orderFeignClient.getOrderById(orderId);

        //创建API对应的request
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl( AlipayConfig.return_payment_url );
        //在公共参数中设置回跳和通知地址
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        Map<String,Object> map = new HashMap<> ();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",0.01);
        map.put("subject",orderInfo.getOrderDetailList().get(0).getSkuName());

        //填充业务参数
        alipayRequest.setBizContent( JSON.toJSONString(map));
        String form =  "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // 将支付信息保存到支付表
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date ());
        paymentInfo.setOrderId(Long.parseLong(orderId));
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus( PaymentStatus.UNPAID.toString());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfoMapper.insert(paymentInfo);


        // 保存支付信息后发送延迟队列，检查支付结果(支付宝回调失败的保险)
//        sendPaymentDelayMessagesx(orderInfo.getOutTradeNo ());//死信
//
        map.put("count",0);//初始化延迟队列的检查次数
       sendPaymentDelayMessage("exchange.delay","routing.delay",map);

        return form;

    }

//    @Override
//    public void sendPaymentDelayMessagesx(String outTradeNo ) {
//        // 死信队列
////       rabbitService.sendDeadMessage("exchange.dead","routing.dead.1",outTradeNo,10, TimeUnit.SECONDS);
//
//////        // 延迟队列
////          rabbitService.sendDeadMessage("exchange.delay","routing.delay",outTradeNo,10, TimeUnit.SECONDS);
//    }

    @Override
    public void sendPaymentDelayMessage(String exchange , String routing , Map<String, Object> map) {


        // 延迟队列
        rabbitService.sendDelayMessage(exchange,routing,JSON.toJSONString(map),5, TimeUnit.SECONDS);
    }

    @Override
    public void sendPaymentMessage(String exchange , String routing, Map<String, Object> map) {
        rabbitService.sendMessage(exchange,routing,JSON.toJSONString(map));
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        //创建条件构造器
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<> ();

        //条件
        queryWrapper.eq ( "out_trade_no", paymentInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.PAID.toString());
        paymentInfo.setCallbackTime(new Date());

        paymentInfoMapper.update(paymentInfo,queryWrapper);

    }

    @Override
    public Map<String, Object> checkAliStatus(String out_trade_no) {
        System.out.println("调用支付宝接口，查询支付状态");

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",out_trade_no);
        request.setBizContent(JSON.toJSONString(map));

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if(response.isSuccess()){
            String tradeStatus = response.getTradeStatus();
            System.out.println("调用成功"+tradeStatus);
            map.put("tradeStatus",tradeStatus);
        } else {
            System.out.println("调用失败");
            map.put("tradeStatus","fail");
        }

        return map;
    }
}

package com.cdq.payment.receiver;

import com.alibaba.fastjson.JSON;
import com.cdq.payment.service.PaymentService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-08 00:44
 **/
@Component
public class PaymentConsumer {


    @Autowired
    PaymentService paymentService;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = "test.exchange",durable = "true",autoDelete = "false"),
            key = {"test.routing"},
            value = @Queue(value = "test.queue",durable = "true",autoDelete = "false")
    ))
    public void a(Message message, Channel channel , String messageStr) throws IOException {

        System.out.println("payment的监听器，消费test队列");

        // 手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

//    /*
//    *死信队列
//    * */
//
//    @SneakyThrows
//    @RabbitListener(queues = "queue.dead.2")
//    public void b(Message message, Channel channel , String messageStr)throws IOException {
//
//        System.out.println("payment的监听器，消费死信队列");
//
//        // 手动确认
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//    }




//     /*
//     *延迟队列
//     * */
//
//    @SneakyThrows
//    @RabbitListener(queues = "queue.delay.1")
//    public void b(Message message, Channel channel , String messageStr)throws IOException {
//
//        System.out.println("payment的监听器，消费延迟队列");
//
//        // 手动确认
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//    }



    /*
    *延迟队列
    * */

    @SneakyThrows
    @RabbitListener(queues = "queue.delay.1")
    public void b(Message message, Channel channel , String messageStr)throws IOException {

        System.out.println(messageStr);
        Map<String,Object> map = new HashMap<> ();
        Map<String,Object> messageMap = JSON.parseObject(messageStr, map.getClass());

        Integer count = (Integer)messageMap.get("count");
        String out_trade_no = (String)messageMap.get("out_trade_no");
        Map<String,Object> aliMap = paymentService.checkAliStatus(out_trade_no);
        String tradeStatus = (String)aliMap.get("tradeStatus");
        // 根据检查结果再次发送延迟检查队列
        if(!tradeStatus.equals("TRADE_SUCCESS")&&!tradeStatus.equals("TRADE_FINISHED")){
            if(null!=count&&count<=7){
                count++;
                messageMap.put("count",count);
                paymentService.sendPaymentDelayMessage("exchange.delay","routing.delay",messageMap);
            }else {
                System.out.println("次数耗尽，停止检查");
            }
        }else {
            System.out.println("已支付完毕，更新支付状态。。。");

        }

        // 手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }


}

package com.cdq.rabbit.raservice.impl;

import com.cdq.rabbit.raservice.RabbitService;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-08 00:03
 **/
@Service
public class RabbitServiceImpl implements RabbitService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void sendMessage(String exchange , String routing ,Object message){
        rabbitTemplate.convertAndSend(exchange,routing,message);
    }

//    @Override
//    public void sendDeadMessage(String exchange, String routing , String message , int ttl , TimeUnit seconds) {
//        rabbitTemplate.convertAndSend ( exchange, routing,message, new MessagePostProcessor () {
//            @Override
//            public Message postProcessMessage(Message message) throws AmqpException {
//                // 设置死信的过期时间
//                message.getMessageProperties ().setExpiration ( ttl*1000+"" );
//                return message;
//            }
//        } );
//    }

    @Override
    public void sendDelayMessage(String exchange , String routing , String message, int ttl , TimeUnit seconds) {
        rabbitTemplate.convertAndSend(exchange,routing,message, new MessagePostProcessor(){
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(ttl*1000);// 设置延迟时间
                return message;
            }
        });
    }


}

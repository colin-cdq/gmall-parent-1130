package com.cdq.payment.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback{

    @Autowired
    RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void MQProducerAckConfig(){
        //将确认回调类注入到rabbitTemplate中
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    @Override
    public void confirm(@Nullable CorrelationData correlationData, boolean isSend, @Nullable String err) {
        // 消息确认方法，只要消息发送必然调用
        System.out.println("消息发送结果："+isSend);
    }

    @Override
    public void returnedMessage(Message message, int isSuccess, String err, String exchange, String routing) {
        // 消息投递回调方法，只有消息投递失败时才调用
        System.out.println("消息投递失败："+exchange+routing+err);
    }

}

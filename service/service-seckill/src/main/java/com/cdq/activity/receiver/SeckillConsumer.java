package com.cdq.activity.receiver;

import com.alibaba.fastjson.JSON;
import com.cdq.activity.service.SeckillService;
import com.cdq.model.user.UserRecode;
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

/**
 * @program: ware-manage
 * @description:
 * @author: cdq
 * @create: 2021-06-15 01:08
 **/
@Component
public class SeckillConsumer {

    @Autowired
    SeckillService seckillService;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = "exchange.direct.seckill.user"),
            key = {"seckill.user"},
            value = @Queue(value = "queue.seckill.user")
    ))
    public void a(Channel channel , Message message, String userRecodeJson) throws IOException {

        UserRecode userRecode = JSON.parseObject(userRecodeJson, UserRecode.class);

        seckillService.seckillOrderStock(userRecode);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);


    }


}

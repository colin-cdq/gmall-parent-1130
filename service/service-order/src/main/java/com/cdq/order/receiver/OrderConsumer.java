package com.cdq.order.receiver;

        import com.alibaba.fastjson.JSON;
        import com.cdq.order.service.OrderService;
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
 * @create: 2021-06-09 14:01
 **/
@Component
public class OrderConsumer {

    @Autowired
    OrderService orderService;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = "exchange.direct.payment.pay",durable = "true",autoDelete = "false"),
            key = {"payment.pay"},
            value = @Queue(value = "queue.payment.pay",durable = "true",autoDelete = "false")
    ))
    public void a(Message message, Channel channel , String messageStr) throws IOException {

        Map<String,Object> map = new HashMap<> ();

        Map<String,Object> messageMap = JSON.parseObject(messageStr, map.getClass());

        System.out.println("order的监听器，消费支付成功");

        // 更新订单状态
        orderService.updatePaymentOrder(messageMap);

        // 手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}

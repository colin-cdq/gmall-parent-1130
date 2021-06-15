package com.cdq.rabbit.raservice;

import java.util.concurrent.TimeUnit;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-08 00:02
 **/
public interface RabbitService {
    void sendMessage(String exchange, String routing, Object message);

//    void sendDeadMessage(String exchange, String routing, String message , int ttl , TimeUnit seconds);

    void sendDelayMessage(String exchange , String routing , String message , int ttl , TimeUnit seconds);
}

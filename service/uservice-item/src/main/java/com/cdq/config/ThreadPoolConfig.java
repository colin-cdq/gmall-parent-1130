package com.cdq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-27 23:34
 **/
@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor a(){
        return new ThreadPoolExecutor(50,100,3, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable> (10000));
    }
}

package com.cdq.uesr.controller;

//import org.redisson.api.RLock;
//import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-27 10:36
 **/
@RestController
public class TestShareController {
    @Autowired
    RedisTemplate redisTemplate;


    Lock lock = new ReentrantLock();// juc 的锁

    @Autowired
//    RedissonClient redissonClient;

    @RequestMapping("getStockbak")
    public String getStockbak() {
//        RLock lock = redissonClient.getLock("stock:lock");
        Integer stock = 0;
        try {
//            lock.lock();
            stock = (Integer) redisTemplate.opsForValue().get("stock");
            stock--;
            System.out.println("目前库存剩余数量:" + stock);
            redisTemplate.opsForValue().set("stock", stock);
        }finally {
            lock.unlock();
        }
        return stock + "";

    }


    @RequestMapping("getStock")
    public String getStock() {

        String lockTag = UUID.randomUUID().toString();
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent("stock:lock", lockTag, 1, TimeUnit.SECONDS);

        if (aBoolean) {
            Integer stock = 0;

            stock = (Integer) redisTemplate.opsForValue().get("stock");

            stock--;

            System.out.println("目前库存剩余数量:" + stock);

            redisTemplate.opsForValue().set("stock", stock);

            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setResultType(Long.class);
            redisScript.setScriptText(luaScript);
            redisTemplate.execute(redisScript, Arrays.asList("stock:lock"), lockTag);

            return stock + "";
        } else {
            return getStock();
        }
    }

}

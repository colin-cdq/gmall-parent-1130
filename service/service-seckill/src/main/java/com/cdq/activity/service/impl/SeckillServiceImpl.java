package com.cdq.activity.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cdq.activity.mapper.SeckillGoodsMapper;
import com.cdq.activity.service.SeckillService;
import com.cdq.model.activity.OrderRecode;
import com.cdq.model.activity.SeckillGoods;
import com.cdq.model.order.OrderDetail;
import com.cdq.model.user.UserRecode;
import com.cdq.rabbit.raservice.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: ware-manage
 * @description:
 * @author: cdq
 * @create: 2021-06-14 03:12
 **/
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RabbitService rabbitService;


    @Override
    public void putSeckillGoods(Long skuId) {

        // 查询秒杀商品
        QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<> ();
        queryWrapper.eq("sku_id",skuId);
        SeckillGoods seckillGoods = seckillGoodsMapper.selectOne(queryWrapper);

        // 放入redis
        if(null!=seckillGoods){
            Integer stockCount = seckillGoods.getStockCount();
            for (int i = 0; i < stockCount; i++) {
                redisTemplate.opsForList().leftPush("stock:"+skuId,skuId);
            }
        }
        redisTemplate.opsForHash().put("seckill:goods",skuId+"",seckillGoods);

        // 发送广播通知
        redisTemplate.convertAndSend("seckillpush",skuId+":1");
    }

    @Override
    public SeckillGoods findBySkuId(Long skuId) {
        QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id",skuId);

        SeckillGoods seckillGoods = seckillGoodsMapper.selectOne(queryWrapper);

        return seckillGoods;
    }

    @Override
    public List<SeckillGoods> findAll() {
        List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(null);

        return seckillGoods;
    }

    @Override
    public void seckillOrder(String userId , Long skuId) {
        UserRecode userRecode = new UserRecode();
        userRecode.setUserId(userId);
        userRecode.setSkuId(skuId);
        rabbitService.sendMessage("exchange.direct.seckill.user","seckill.user", JSON.toJSONString(userRecode));
    }

    @Override
    public void seckillOrderStock(UserRecode userRecode) {
        //userId、skuId做一个锁，防止用户短时间内多次抢购
        String userId = userRecode.getUserId();
        Long skuId = userRecode.getSkuId();
        Integer stock = (Integer)redisTemplate.opsForList().rightPop("stock:" + skuId);
        if(null!=stock&&stock>0){
            // 抢购成功,生成预订单
            OrderRecode recode = new OrderRecode();
            SeckillGoods seckillGoods = (SeckillGoods)redisTemplate.opsForHash().get("seckill:goods",skuId+"");
            recode.setUserId(userId);
            recode.setSeckillGoods(seckillGoods);
            recode.setNum(1);

            redisTemplate.opsForHash().put("seckill:orders",userId,recode);// 保存预订单
        }else {
            // 商品已经售罄，发出售罄广播
            redisTemplate.convertAndSend("seckillpush",skuId+":0");
        }
    }

    @Override
    public String checkOrder(String userId) {
        String orderId = (String)redisTemplate.opsForHash().get("seckill:orders:users", userId);
        return orderId;
    }

    @Override
    public OrderRecode checkOrderRecode(String userId) {

        OrderRecode orderRecode = (OrderRecode)redisTemplate.opsForHash().get("seckill:orders",userId);// 查看预订单

        return orderRecode;
    }

    @Override
    public List<OrderDetail> getOrderDetailList(String userId) {

        OrderRecode orderRecode = (OrderRecode)redisTemplate.opsForHash().get("seckill:orders",userId);// 查看预订单

        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();

        List<OrderDetail> orderDetails = new ArrayList<> ();

        OrderDetail orderDetail = new OrderDetail();

        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setOrderPrice(seckillGoods.getPrice());
        orderDetail.setSkuNum(1);
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());

        orderDetails.add(orderDetail);
        return orderDetails;
    }

}

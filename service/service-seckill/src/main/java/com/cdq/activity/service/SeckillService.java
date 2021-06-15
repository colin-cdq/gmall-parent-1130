package com.cdq.activity.service;

import com.cdq.model.activity.OrderRecode;
import com.cdq.model.activity.SeckillGoods;
import com.cdq.model.order.OrderDetail;
import com.cdq.model.user.UserRecode;

import java.util.List;

/**
 * @program: ware-manage
 * @description:
 * @author: cdq
 * @create: 2021-06-14 03:12
 **/
public interface SeckillService {
    void putSeckillGoods(Long skuId);

    SeckillGoods findBySkuId(Long skuId);

    List<SeckillGoods> findAll();

    void seckillOrder(String userId , Long skuId);

    void seckillOrderStock(UserRecode userRecode);

    String checkOrder(String userId);

    OrderRecode checkOrderRecode(String userId);

    List<OrderDetail> getOrderDetailList(String userId);
}

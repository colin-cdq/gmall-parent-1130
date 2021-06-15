package com.cdq.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cdq.client.CartFeignClient;
import com.cdq.feign.ProductFeignClient11111;
import com.cdq.model.cart.CartInfo;
import com.cdq.model.enums.OrderStatus;
import com.cdq.model.enums.ProcessStatus;
import com.cdq.model.order.OrderDetail;
import com.cdq.model.order.OrderInfo;
import com.cdq.model.product.SkuInfo;
import com.cdq.model.ware.WareOrderTask;
import com.cdq.model.ware.WareOrderTaskDetail;
import com.cdq.order.mapper.OrderDetailMapper;
import com.cdq.order.mapper.OrderInfoMapper;
import com.cdq.order.service.OrderService;
import com.cdq.rabbit.raservice.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-06 20:52
 **/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    ProductFeignClient11111 productFeignClient;

     @Autowired
    RabbitService rabbitService;

    @Override
    public String genTradeNo(String userId) {
        // 生成交易码
        String tradeNo = UUID.randomUUID().toString();

        // 将交易马保存到缓存
        redisTemplate.opsForValue().set("user:"+userId+":tradeNo",tradeNo);

        // 返回交易码给结算页面
        return tradeNo;
    }



    @Override
    public OrderInfo getTradeOrder(String userId) {
        OrderInfo orderInfo = new OrderInfo();

        List<CartInfo> cartInfos =  cartFeignClient.getTradeOrder(userId);

        if(null!=cartInfos&&cartInfos.size()>0)
        {
            List<OrderDetail> orderDetails = new ArrayList<> ();
            for (CartInfo cartInfo : cartInfos) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetails.add(orderDetail);
            }
            orderInfo.setOrderDetailList(orderDetails);
        }
        return orderInfo;
    }

    @Override
    public boolean checkTradeNo(String userId, String tradeNo) {

        boolean b = false;

        //在缓存中查询
        String checkNo = (String)redisTemplate.opsForValue().get("user:" + userId + ":tradeNo");

        //判断是否存在
        if(!StringUtils.isEmpty(checkNo)&&tradeNo.equals(checkNo)){
            b = true;
            // 用过之后删除交易码
            redisTemplate.delete("user:" + userId + ":tradeNo");
        }

        return b;
    }

//    public static void main(String[] args) {
//        String yyyyMMddHHmmss = new SimpleDateFormat ("yyyyMMddHHmmss").format(new Date());
//        System.out.println(yyyyMMddHHmmss);
//    }


//
    @Override
    public String submitOrder(OrderInfo orderInfo) {

        //订单创建时间
        orderInfo.setCreateTime(new Date());
        //Calendar日历，时钟，当前时间
        Calendar calendar = Calendar.getInstance();
        //当前时间加一天
        calendar.add(Calendar.DATE,1);
        //获取对象
        Date time = calendar.getTime();
        // 24小时候过期
        orderInfo.setExpireTime(time);
        //订单状态
        orderInfo.setOrderStatus( OrderStatus.UNPAID.getComment());
        String yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        System.out.println(yyyyMMddHHmmss);
        String out_trade_no = "atguigu"+System.currentTimeMillis()+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // 当前交易的订单号，需要保证唯一不重复
        orderInfo.setOutTradeNo(out_trade_no);
        orderInfo.setProcessStatus( ProcessStatus.UNPAID.getComment());
        //总金额
        orderInfo.setTotalAmount(getTotalAmount(orderInfo.getOrderDetailList()));

        //保存在orderInfo表中
        orderInfoMapper.insert(orderInfo);

        Long orderId = orderInfo.getId();

        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            orderDetail.setOrderId(orderId);
            // 验价，价格要和购物车列表中的对表
            SkuInfo currentSku = productFeignClient.getSkuById(orderDetail.getSkuId());
            BigDecimal priceCheck = currentSku.getPrice();
            BigDecimal orderPrice = orderDetail.getOrderPrice();
            Integer skuNum = orderDetail.getSkuNum();

            // 验库存
            // 一旦价格和库存发生变化，方法回滚，取消订单提交业务，用户重新回到购物车确认
            orderDetailMapper.insert(orderDetail);

            // 删除购物车中的信息

        }

        return orderId+"";
    }

    @Override
    public OrderInfo getOrderById(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(Long.parseLong(orderId));

        QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(queryWrapper);
        orderInfo.setOrderDetailList(orderDetails);
        return orderInfo;
    }

    @Override
    public void updatePaymentOrder(Map<String, Object> messageMap) {
        //从messageMap获取参数
        String out_trade_no = (String)messageMap.get("out_trade_no");
        String trade_no = (String)messageMap.get("trade_no");

        //更新订单状态
        //创建条件构造器
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<> ();
        //条件
        queryWrapper.eq("out_trade_no",out_trade_no);
        //创建OrderInfo对象
        OrderInfo orderInfo = new OrderInfo ();
        //修改内容
        orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
        orderInfo.setTradeBody(trade_no);
        //更新
        orderInfoMapper.update(orderInfo,queryWrapper);

        // 当订单更新完毕之后，还要通知库存系统，锁定库存
        //创建WareOrderTask 设置条件
        WareOrderTask wareOrderTask = new WareOrderTask();
        OrderInfo orderInfoForWare = getOrderByOutTradeNo(out_trade_no);
        wareOrderTask.setOrderId(orderInfoForWare.getId()+"");
        List<WareOrderTaskDetail> wareOrderTaskDetails = new ArrayList<>();
        for (OrderDetail orderDetail : orderInfoForWare.getOrderDetailList()) {
            WareOrderTaskDetail wareOrderTaskDetail = new WareOrderTaskDetail();
            wareOrderTaskDetail.setSkuId(orderDetail.getSkuId()+"");
            wareOrderTaskDetail.setSkuNum(orderDetail.getSkuNum());
            wareOrderTaskDetails.add(wareOrderTaskDetail);
        }
        //加入条件来通知库存
        wareOrderTask.setDetails(wareOrderTaskDetails);
        rabbitService.sendMessage("exchange.direct.ware.stock","ware.stock", JSON.toJSONString(wareOrderTask));


    }

    private OrderInfo getOrderByOutTradeNo(String out_trade_no) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",out_trade_no);
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);

        QueryWrapper<OrderDetail> queryWrapperDetail = new QueryWrapper<>();

        queryWrapperDetail.eq("order_id",orderInfo.getId());

        List<OrderDetail> orderDetails = orderDetailMapper.selectList(queryWrapperDetail);

        orderInfo.setOrderDetailList(orderDetails);

        return orderInfo;
    }

    private BigDecimal getTotalAmount(List<OrderDetail> orderDetailList) {

        BigDecimal totalAmount = new BigDecimal("0");

        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount = totalAmount.add(orderDetail.getOrderPrice());
        }

        return totalAmount;
    }


}

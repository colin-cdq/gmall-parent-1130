package com.cdq.uesr.controller;

import com.cdq.model.order.OrderDetail;
import com.cdq.model.order.OrderInfo;
import com.cdq.model.user.UserAddress;
import com.cdq.order.OrderFeignClient;
import com.cdq.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-06 18:34
 **/
@Controller
public class OrderController {

    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    UserFeignClient userFeignClient;

    @RequestMapping("trade.html")
    public String trade(Model model){

        // 订单数据，自定义一个方法来获取
        OrderInfo orderInfo = orderFeignClient.getTradeOrder();

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        // 收货人信息,从购物车获取
        List<UserAddress> userAddresses = userFeignClient.getUserAddresses();

        //把对应的数据返回前端
        model.addAttribute("detailArrayList",orderDetailList);
        model.addAttribute("userAddressList",userAddresses);

        orderInfo.setConsignee(userAddresses.get(0).getConsignee());
        orderInfo.setConsigneeTel(userAddresses.get(0).getPhoneNum());
        model.addAttribute("order",orderInfo);
        //结算页面的结算码
        model.addAttribute("tradeNo",orderFeignClient.genTradeNo());
        return "order/trade";
    }
}

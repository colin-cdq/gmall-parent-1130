package com.cdq.uesr.controller;


import com.cdq.common.util.MD5;
import com.cdq.common.util.Result;
import com.cdq.model.activity.SeckillGoods;
import com.cdq.model.order.OrderDetail;
import com.cdq.model.user.UserAddress;
import com.cdq.seckill.client.SeckillFeignClient;
import com.cdq.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @program: ware-manage
 * @description:
 * @author: cdq
 * @create: 2021-06-14 05:34
 **/
@Controller
public class SeckillController {
    @Autowired
    SeckillFeignClient seckillFeignClient;

    @Autowired
    UserFeignClient userFeignClient;

    @RequestMapping("seckill/{skuId}.html")
    public String seckillItem(@PathVariable("skuId") Long skuId, Model model){
        SeckillGoods seckillGoods =  seckillFeignClient.findBySkuId(skuId);
        model.addAttribute("item",seckillGoods);

        return "seckill/item";
    }

    @RequestMapping("seckill.html")
    public String seckillHtml(Model model){
        List<SeckillGoods> seckillGoods =  seckillFeignClient.findAll();
        model.addAttribute("list",seckillGoods);

        return "seckill/index";
    }

    @RequestMapping("/seckill/queue.html")
    public String queue(Long skuId, String skuIdStr, Model model, HttpServletRequest request){

        // 校验抢购码
        String userId = request.getHeader("userId");
        String encrypt = MD5.encrypt(userId + skuId);

        if(!StringUtils.isEmpty(skuIdStr)&&encrypt.equals(skuIdStr)){
            model.addAttribute("skuId",skuId);
            model.addAttribute("skuIdStr",encrypt);
            return "seckill/queue";
        }else {
            return "seckill/fail";
        }

    }


    @RequestMapping("/seckill/trade.html")
    public String seckillTrade(Model model, HttpServletRequest request){

        String userId = request.getHeader("userId");

        List<OrderDetail> orderDetailList = seckillFeignClient.getOrderDetailList();

        // 收货人信息
        List<UserAddress> userAddresses = userFeignClient.getUserAddresses();

        model.addAttribute("detailArrayList",orderDetailList);
        model.addAttribute("userAddressList",userAddresses);


        return "seckill/trade";
    }
}

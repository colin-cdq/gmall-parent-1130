package com.cdq.uesr.controller;

import com.cdq.model.order.OrderInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-07 01:12
 **/

@Controller
public class PaymentController {


    @RequestMapping("pay.html")
    public String pay(String orderId, Model model){
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(Long.parseLong(orderId));

        model.addAttribute("orderInfo",orderInfo);
        return "payment/pay";
    }
}

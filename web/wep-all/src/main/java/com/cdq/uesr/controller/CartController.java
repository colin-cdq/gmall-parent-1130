package com.cdq.uesr.controller;

import com.cdq.client.CartFeignClient;
import com.cdq.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-02 23:44
 **/
@Controller
public class CartController {

    @Autowired
    CartFeignClient cartFeignClient;

    @RequestMapping("cart/cart.html")
    public String cartHtml(HttpServletRequest request,Model model) {
        String userId = request.getHeader("userId");
        String userTempId = request.getHeader("userTempId");
        return "cart/index";
    }


//    @RequestMapping("addCart.html")
//    public String addCart(Long skuId, Long skuNum) {
//
//
//        // 调用后台cartService微服务
//        CartInfo cartInfo = cartFeignClient.addCart(skuId, skuNum);
//
//        //点击购物车式调用该方法，并渲染到index中
////        return "cart/addCart";
//
//        // 通过url将购物车页面参数传递过去addCart.html?skuName=111&sku...
//        return "redirect:http://cart.gmall.com:8300/cart/addCart.html?skuName=" +cartInfo.getSkuName() + "&skuDefaultImg=" + cartInfo.getImgUrl();
//
//    }

    @RequestMapping("addCart.html")
    public String addCart(HttpServletRequest request, Long skuId, Long skuNum) {

        String userId = request.getHeader("userId");
        String userTempId = request.getHeader("userTempId");

        // 调用后台cartService微服务
        CartInfo cartInfo = cartFeignClient.addCart(skuId, skuNum);
//
//        // 通过url将购物车页面参数传递过去addCart.html?skuName=111&sku...
        return "redirect:http://cart.gmall.com:8300/cart/addCart.html?skuName=" + URLEncoder.encode(cartInfo.getSkuName()) + "&skuDefaultImg=" + URLEncoder.encode(cartInfo.getImgUrl());

          //   return "cart/addCart";
    }

}

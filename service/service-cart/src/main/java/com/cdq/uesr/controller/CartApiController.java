package com.cdq.uesr.controller;

import com.cdq.common.util.AuthContextHolder;
import com.cdq.common.util.Result;
import com.cdq.model.cart.CartInfo;
import com.cdq.uesr.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-03 01:06
 **/
@RestController
@RequestMapping("api/cart")
@CrossOrigin
public class CartApiController {
    @Autowired
    CartService cartService;


    @RequestMapping("cartList")
    public Result cartList(HttpServletRequest request) {
        // 通过单点登录体系获得用户id

         String userId = request.getHeader("userId");
//        String userId = AuthContextHolder.getUserId ( request );
        String userTempId = request.getHeader("userTempId");
        userId = "3";
        List<CartInfo> cartInfos = cartService.cartList(userId);
        return Result.ok(cartInfos);
    }

    @RequestMapping("addCart/{skuId}/{skuNum}")
        public CartInfo addCart(HttpServletRequest request, @PathVariable("skuId") Long skuId, @PathVariable("skuNum") Long skuNum) {

        // 通过单点登录体系获得用户id
        String userId = request.getHeader("userId");
//        String userId = AuthContextHolder.getUserId ( request );
        String userTempId = request.getHeader("userTempId");
        //测试通过后把userId=1 删除
        userId = "3";
        CartInfo cartInfo = cartService.addCart(skuId, skuNum, userId);
        return cartInfo;
    }



    @RequestMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(HttpServletRequest request,@PathVariable("skuId") Long skuId, @PathVariable("isChecked") String isChecked) {
        // 通过单点登录体系获得用户id
        String userId = request.getHeader("userId");
        String userTempId = request.getHeader("userTempId");
        cartService.checkCart(userId,skuId,isChecked);
        return Result.ok();
    }

    @RequestMapping("getTradeOrder/{userId}")
    List<CartInfo> getTradeOrder(@PathVariable("userId") String userId){
        return cartService.getTradeOrder(userId);
    }

    public static void main(String[] args) {

        // 基本类型数据 1 1l 1.0d 1.0f
        // 运算符号 + - * / > < =

        // 初始化
        BigDecimal b1 = new BigDecimal("0.01");
        BigDecimal b2 = new BigDecimal("0.01");

        System.out.println(b1);
        System.out.println(b2);

        BigDecimal b3 = new BigDecimal("6");
        BigDecimal b4 = new BigDecimal("7");

        System.out.println(b3);
        System.out.println(b4);

        // 运算
        BigDecimal add = b3.add(b4);
        System.out.println(add);

        BigDecimal subtract = b3.subtract(b4);

        System.out.println(subtract);

        BigDecimal multiply = b3.multiply(b4);

        System.out.println(multiply);

        // 取近似值
        BigDecimal divide = b3.divide(b4,14,BigDecimal.ROUND_HALF_DOWN);

        System.out.println(divide);

        BigDecimal add1 = b1.add(b2);

        System.out.println(add1);

        BigDecimal bigDecimal = add1.setScale(10, BigDecimal.ROUND_HALF_DOWN);

        System.out.println(bigDecimal);

        // 比较
        int i = b1.compareTo(b2);// -1 0 1

        System.out.println(i);

    }
}

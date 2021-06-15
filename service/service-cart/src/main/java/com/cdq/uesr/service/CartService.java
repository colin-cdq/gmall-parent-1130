package com.cdq.uesr.service;

import com.cdq.model.cart.CartInfo;

import java.util.List;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-03 01:12
 **/
public interface CartService {

    CartInfo addCart(Long skuId , Long skuNum , String userId);

    List<CartInfo> cartList(String userId);

    void checkCart(String userId , Long skuId , String isChecked);

    List<CartInfo> getTradeOrder(String userId);
}

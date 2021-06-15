package com.cdq.client;

import com.cdq.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-03 00:52
 **/
@FeignClient(value = "service-cart")
public interface CartFeignClient {

    @RequestMapping("api/cart/addCart/{skuId}/{skuNum}")
    CartInfo addCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Long skuNum);


    @RequestMapping("api/cart/getTradeOrder/{userId}")
    List<CartInfo> getTradeOrder(@PathVariable("userId") String userId);
}

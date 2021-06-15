package com.cdq.product.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-21 21:27
 **/

@FeignClient(value = "uservice-item")
public interface ItemFeignClient {

    @RequestMapping("api/item/{skuId}")
    Map<String,Object> item(@PathVariable("skuId") Long skuId);
}

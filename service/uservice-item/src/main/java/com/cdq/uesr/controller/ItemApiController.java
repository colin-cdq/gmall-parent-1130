package com.cdq.uesr.controller;

import com.cdq.uesr.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-24 21:16
 **/
@RestController
@CrossOrigin
@RequestMapping("api/item")
public class ItemApiController {
    @Autowired
    ItemService itemService;

    @RequestMapping("{skuId}")
    Map<String,Object> item(@PathVariable("skuId") Long skuId){

        Map<String,Object> map = itemService.item(skuId);

        a();

        return map;
    }

    public void a(){}
}

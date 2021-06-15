package com.cdq.uesr.controller;

import com.cdq.product.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-21 01:35
 **/
@Controller
public class ItemController {

    @Autowired
     ItemFeignClient itemFeignClient;

    @RequestMapping("{skuId}.html")
    public String index(Model model,@PathVariable("skuId") Long skuId, HttpServletRequest request){
        System.out.println(request.getRemoteAddr()+"同学访问了商品详情");
        Map<String,Object> map  = itemFeignClient.item(skuId);// 访问后台item服务加载数据
        model.addAllAttributes(map);
        return "item/index";
    }


    @RequestMapping("test")
    public String test(Model model){
        String url ="hello world";
        model.addAttribute ( "my",url );

        List<String> list = new ArrayList<> ();

        for (int i = 0; i < 5; i++) {
            list.add("元素"+i);
        }
        model.addAttribute("list",list);

        model.addAttribute("num","奥特曼");
        return "testTh";
    }
}

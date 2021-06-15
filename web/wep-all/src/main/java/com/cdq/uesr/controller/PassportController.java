package com.cdq.uesr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-04 19:09
 **/

@Controller
public class PassportController {

    @RequestMapping("login.html")
    public String login(HttpServletRequest request, String originUrl, Model model){
        String requestURI = request.getRequestURI();
        StringBuffer requestURL = request.getRequestURL();
        String contextPath = request.getContextPath();
        //获得从定向的地址
        String queryString = request.getQueryString();
        //获得等号位置
        int i = queryString.indexOf("=");
        //截取位置从=后一位开始截取
        originUrl = queryString.substring(i + 1);
        model.addAttribute("originUrl",originUrl);
        return "login";
    }
}

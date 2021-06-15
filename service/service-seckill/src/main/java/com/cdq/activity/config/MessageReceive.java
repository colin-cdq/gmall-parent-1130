package com.cdq.activity.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MessageReceive {

    public void receiveMessage(String message){

        System.out.println("监听方法："+message);

        message = message.replaceAll("\"","");
        String[] split = StringUtils.split(message, ":");

        // 更新服务器的状态
        CacheHelper.put("sku:"+split[0],split[1]);// 30:1(30号sku有货) 30:0(30号sku无货)

    }

}

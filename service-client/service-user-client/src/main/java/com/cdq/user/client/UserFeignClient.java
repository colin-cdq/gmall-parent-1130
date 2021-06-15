package com.cdq.user.client;

import com.cdq.model.user.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-04 00:46
 **/
@FeignClient(value = "service-user")
public interface UserFeignClient {

    @RequestMapping("api/user/passport/verify/{token}")
    Map<String,Object> verify(@PathVariable("token") String token);


    @RequestMapping("api/user/passport/getUserAddresses")
    List<UserAddress> getUserAddresses();
}

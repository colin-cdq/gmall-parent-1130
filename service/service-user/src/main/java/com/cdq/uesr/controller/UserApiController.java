package com.cdq.uesr.controller;



import com.cdq.common.util.Result;
import com.cdq.model.user.UserAddress;
import com.cdq.model.user.UserInfo;
import com.cdq.uesr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-04 00:54
 **/
@RestController
@RequestMapping("api/user/passport")
public class UserApiController {

    @Autowired
    UserService userService;

    @RequestMapping("verify/{token}")
    Map<String,Object> verify(@PathVariable("token") String token){
        Map<String,Object> map = userService.verify(token);
            return map;
    }

    @RequestMapping("login")
    Result login(@RequestBody UserInfo userInfo){
        // 调用登录业务层
        UserInfo userInfoResult = userService.login(userInfo);
        //判断返回的数据是否为空
        if(null!=userInfoResult){
            return Result.ok(userInfoResult);
        }else {
            return Result.fail("用户名或者密码错误");
        }
    }

    @RequestMapping("getUserAddresses")
    List<UserAddress> getUserAddresses(HttpServletRequest request){

        String userId = request.getHeader("userId");


        List<UserAddress> userAddresses = userService.getUserAddresses ( userId );
        return userAddresses;
    }
}

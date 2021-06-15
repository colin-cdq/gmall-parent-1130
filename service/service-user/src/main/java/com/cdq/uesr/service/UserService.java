package com.cdq.uesr.service;

import com.cdq.model.user.UserAddress;
import com.cdq.model.user.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-04 18:39
 **/
public interface UserService  {
    Map<String, Object> verify(String token);

    UserInfo login(UserInfo userInfo);

    List<UserAddress> getUserAddresses(String userId);
}

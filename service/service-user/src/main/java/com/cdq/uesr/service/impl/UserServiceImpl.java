package com.cdq.uesr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cdq.common.util.MD5;
import com.cdq.model.user.UserAddress;
import com.cdq.model.user.UserInfo;
import com.cdq.uesr.mapepr.UserInfoMapper;
import com.cdq.uesr.mapepr.userAddressMapper;
import com.cdq.uesr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-04 18:32
 **/
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
   userAddressMapper userAddressMapper;

    @Override
    public Map<String, Object> verify(String token) {
        Map<String, Object> map = new HashMap<> ();

        //获取缓存的数据
        UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get("user:login:" + token);
        //缓存不等于null时，赋值
        if (null != userInfo) {
            //把值放到map中
            map.put("success", "success");
            map.put("userId", userInfo.getId() + "");
        }
        //返回
        return map;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        String loginName = userInfo.getLoginName();
        String passwd = userInfo.getPasswd();

        //密码使用配置文件md5加密
        String encryptPasswd = MD5.encrypt(passwd);

        //创建构造器
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name", loginName);
        queryWrapper.eq("passwd", encryptPasswd);

        //查询数据库是否有数据，数据只有一条
        UserInfo userInfoDb = userInfoMapper.selectOne(queryWrapper);

        //判断
        if (null != userInfoDb) {
            // 登录成功
            // 生成token
            String token = UUID.randomUUID().toString();
            // 保存token
            redisTemplate.opsForValue().set("user:login:" + token, userInfoDb);
            //给数据库的token赋值
            userInfoDb.setToken(token);

            return userInfoDb;
        } else {
            return null;
        }
    }

    @Override
    public List<UserAddress> getUserAddresses(String userId) {
        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id",userId);

        List<UserAddress> userAddresses = userAddressMapper.selectList(queryWrapper);

        return userAddresses;
    }
}

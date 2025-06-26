package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0.0
 * @program: sky-take-out
 * @className: UserServiceImpl
 * @description:
 * @author: Lin
 * @create: 2025/4/13 21:07
 **/
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    // 微信服务接口地址
    private final static String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());
        // 判断openid是否为空, 如果为空表示登录失败, 抛出业务异常
        if (openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);
        // 如果是新用户, 自动完成注册
        if (user==null){
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }


        // 返回用户对象
        return user;
    }

    /**
     * 调用微信接口服务, 获取用户的openid
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        // 调用微信接口服务, 获得当前微信用户的openid
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid",weChatProperties.getAppid());
        paramMap.put("secret",weChatProperties.getSecret());
        paramMap.put("js_code",code);
        paramMap.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, paramMap);
        log.info("微信接口返回的json数据:{}", json);
        JSONObject jsonObject = JSONObject.parseObject(json);
        log.info("调用JSONObject.parseObject()格式化后的数据:{}", jsonObject);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
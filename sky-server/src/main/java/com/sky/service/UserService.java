package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * @program: sky-take-out
 * @interfaceName: UserService
 * @description:
 * @author: Lin
 * @create: 2025-04-13 19:00
 **/
public interface UserService {

    User wxLogin(UserLoginDTO userLoginDTO);
}

package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * @program: sky-take-out
 * @interfaceName: UserMapper
 * @description:
 * @author: Lin
 * @create: 2025-04-13 21:23
 **/
@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 查询当日新增用户数据
     * @param map
     * @return
     */
    Integer sumNewUserByMap(Map map);

    /**
     * 查询当日用户总量
     * @param map
     * @return
     */
    Integer sumTotalUserByMap(Map map);
}

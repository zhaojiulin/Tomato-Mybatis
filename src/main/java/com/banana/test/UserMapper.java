package com.banana.test;

import com.banana.orm.anno.Param;
import com.banana.orm.anno.Select;

import java.util.List;

public interface UserMapper {
    @Select("select * from user")
    public List<User> getUserList();

    @Select("select * from user where username = #{username}")
    public List<User> getUserByName(@Param("username") String username);

    @Select("select * from user where username = #{username}")
    public User getUser(@Param("username") String username);

    @Select("update user set age = #{age} where username = #{username}")
    public Integer updateAge(@Param("username") String username, @Param("age") Integer age);
}

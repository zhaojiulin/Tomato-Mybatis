package com.banana;

import com.banana.orm.anno.Param;
import com.banana.orm.anno.SqlExec;

import java.util.List;

public interface UserMapper {
    @SqlExec("select * from user")
    public List<User> getUserList();

    @SqlExec("select * from user where username = #{username}")
    public List<User> getUserByName(@Param("username") String username);

    @SqlExec("select * from user where username = #{username}")
    public User getUser(@Param("username") String username);

    @SqlExec("update user set age = #{age} where username = #{username}")
    public Integer updateAge(@Param("username") String username, @Param("age") Integer age);
}

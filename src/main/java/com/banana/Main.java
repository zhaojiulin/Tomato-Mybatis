package com.banana;

import com.banana.orm.TomatoMapperProxyFactory;
import com.banana.test.UserMapper;

public class Main {
    public static void main(String[] args) {
        TomatoMapperProxyFactory mapperProxyFactory = new TomatoMapperProxyFactory();
        UserMapper userMapper = mapperProxyFactory.getProxy(UserMapper.class);
        System.out.println(userMapper.updateAge("张三", 66));
    }
}
package com.banana;

import com.banana.orm.DatabaseConnectionPool;
import com.banana.orm.TomatoMapperProxyFactory;

public class Main {
    public static void main(String[] args) {
        TomatoMapperProxyFactory mapperProxyFactory = new TomatoMapperProxyFactory();
        UserMapper userMapper = mapperProxyFactory.getProxy(UserMapper.class);
        System.out.println(userMapper.getUserList());
        DatabaseConnectionPool.getInstance().shutdown();
    }
}
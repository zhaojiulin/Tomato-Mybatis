package com.banana.orm;

import com.banana.orm.anno.Param;
import com.banana.orm.anno.Select;
import com.banana.orm.handle.BasicTypeHandle;
import com.banana.orm.parsing.ParamMappingTokenHandle;
import com.banana.orm.parsing.SqlParseResult;
import com.banana.orm.result.BasicResultTypeHandler;
import com.banana.orm.result.ListResultTypeHandle;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.List;

public class TomatoMapperProxyFactory {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getProxy(Class<T> clazz) {
        // 链接->>数据库连接池
        // 构造预编译
        // 执行sql
        // 根据方法返回类型封装数据

        Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            // sql
            Select annotation = method.getAnnotation(Select.class);
            String sql = annotation.value();
            ParamMappingTokenHandle paramMappingTokenHandle = new ParamMappingTokenHandle();
            SqlParseResult sqlParseResult = paramMappingTokenHandle.handleToken(sql);
            DatabaseConnectionPool databaseConnectionPool = DatabaseConnectionPool.getInstance();
            Connection connection = databaseConnectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlParseResult.getParseSql());
            // 参数映射关系
            HashMap<String, Object> paramMapping = paramValueMapping(method, args);
            for (int i = 0; i < sqlParseResult.getParamList().size(); i++) {
                Object object = paramMapping.get(sqlParseResult.getParamList().get(i));
                BasicTypeHandle<?> basicTypeHandle = new BasicTypeHandle<>(object.getClass());
                basicTypeHandle.setParameter(preparedStatement, i + 1, object);
            }
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            Object result = getResult(method, resultSet);
            databaseConnectionPool.releaseConnection(connection);
            return result;
        });
        T proxyInstance1 = (T) proxyInstance;
        return proxyInstance1;
    }

    private <T> Object getResult(Method method, ResultSet resultSet) throws SQLException {
        Class<?> methodReturnType = method.getReturnType();
        Object result = null;
        if (isBasicType(methodReturnType)) {
            BasicResultTypeHandler basicResultTypeHandler = new BasicResultTypeHandler<>(methodReturnType);
            result = basicResultTypeHandler.handle(resultSet);
        } else if (List.class.isAssignableFrom(methodReturnType)) {
            Class classType = null;
            Type genericReturnType = method.getGenericReturnType();
            boolean isList = false;
            if (genericReturnType instanceof Class) {
                classType = (Class) genericReturnType;
            } else if (genericReturnType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
                classType = (Class) actualTypeArguments[0];
                isList = true;
            }
            ListResultTypeHandle listResultTypeHandle = new ListResultTypeHandle<>(classType);
            List<T> handle = listResultTypeHandle.handle(resultSet);
            if (!isList && handle.size() > 0) {
                throw new RuntimeException("Excepted one but found " + handle.size());
            }
            result = isList ? handle : handle.get(0);
        }
        return result;
    }

    private static HashMap<String, Object> paramValueMapping(Method method, Object[] args) {
        HashMap<String, Object> paramMapping = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            paramMapping.put(parameter.getName(), args[i]);
            Param parameterAnnotation = parameter.getAnnotation(Param.class);
            paramMapping.put(parameterAnnotation.value(), args[i]);

        }
        return paramMapping;
    }

    private boolean isBasicType(Class<?> type) {
        return type.isPrimitive() ||
                type == String.class ||
                type == Integer.class ||
                type == Long.class ||
                type == Double.class ||
                type == Float.class ||
                type == Boolean.class ||
                type == BigDecimal.class ||
                type == Date.class ||
                type == Timestamp.class;
    }
}

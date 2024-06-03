package org.example.rpcstarter.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 模拟服务的动态代理，这里不需要真的去发送请求，只需要根据调用的方法的返回值构造一个默认的结果就行
 */
public class MockServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Class<?> type = method.getReturnType();
        if(type.isPrimitive()){
            //如果方法的返回值类型是基本数据类型
            if(type == int.class){
               return 0;
            } else if (type == byte.class) {
                return 0;
            } else if (type == short.class) {
                return (short) 0;
            } else if (type == char.class) {
                return "";
            } else if (type == long.class) {
                return 0L;
            } else if (type == float.class) {
                return 0.0;
            } else if (type == double.class) {
                return 0.0;
            } else if (type == boolean.class) {
                return true;
            }
        }
        //如果方法的返回值类型是引用类型
        return null;
    }
}

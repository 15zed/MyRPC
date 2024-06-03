package org.example.rpcstarter.annotation;


import org.example.rpcstarter.constant.RPCConstants;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解用于服务提供者声明服务
 * 在提供服务的类上使用
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RPCService {
    /**
     * 服务的接口类
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 服务的名称
     * @return
     */
    String serviceName() default "";

    /**
     * 服务版本
     * @return
     */
    String serviceVersion() default RPCConstants.DEFAULT_SERVICE_VERSION;

    /**
     * 服务权重
     * @return
     */
    int weight() default 2;
}

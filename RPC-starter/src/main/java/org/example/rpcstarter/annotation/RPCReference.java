package org.example.rpcstarter.annotation;


import org.example.rpcstarter.constant.LoadBalanceConstants;
import org.example.rpcstarter.constant.RPCConstants;
import org.example.rpcstarter.constant.RetryPolicyConstants;
import org.example.rpcstarter.constant.TolerantConstants;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解用于服务消费者标识和注入远程服务引用
 * 在需要注入服务代理对象的字段上使用
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RPCReference {
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
     * 负载均衡
     * @return
     */
    String loadBalancer() default LoadBalanceConstants.ROUND_ROBIN;

    /**
     * 重试策略
     * @return
     */
    String retryPolicy() default RetryPolicyConstants.FIXED_WAIT;

    /**
     * 容错机制
     * @return
     */
    String tolerantPolicy() default TolerantConstants.FAIL_FAST;

    /**
     * 开启mock
     * @return
     */
    boolean isMock() default false;
}

package org.example.rpcstarter.annotation;

import org.example.rpcstarter.start.RPCConsumer;
import org.example.rpcstarter.start.RPCInit;
import org.example.rpcstarter.start.RPCProvider;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解用来启动RPC框架,进行框架初始化
 * 在springboot启动类上使用
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({RPCConsumer.class, RPCInit.class, RPCProvider.class})
public @interface EnableRPC {
    /**
     * 是否启动web服务器
     * @return
     */
   boolean openServer() default true;
}

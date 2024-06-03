package org.example.rpcstarter.start;


import org.example.rpcstarter.RpcApplication;
import org.example.rpcstarter.annotation.EnableRPC;
import org.example.rpcstarter.config.RegistryConfig;
import org.example.rpcstarter.config.RpcConfig;
import org.example.rpcstarter.registry.Registry;
import org.example.rpcstarter.registry.RegistryFactory;
import org.example.rpcstarter.server.http.VertxHttpServer;
import org.example.rpcstarter.util.ConfigUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;


/**
 * RPC全局启动类，spring框架初始化的时候，获取@EnableRPC注解的属性，初始化RPC框架
 */
public class RPCInit implements ImportBeanDefinitionRegistrar {

    /**
     * 是否已经初始化过注册器了
     */
    private boolean isInitRegistry = false;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableRPC.class.getName());
        boolean openServer = (boolean) attributes.get("openServer");
        RpcApplication.init();
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        if(openServer){
            VertxHttpServer vertxHttpServer = new VertxHttpServer();
            vertxHttpServer.start(rpcConfig.getServerPort());
        }else {
            System.out.println("不启动server");
        }
    }
}

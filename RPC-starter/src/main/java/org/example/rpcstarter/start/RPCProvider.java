package org.example.rpcstarter.start;


import org.example.rpcstarter.RpcApplication;
import org.example.rpcstarter.annotation.RPCService;
import org.example.rpcstarter.common.ServiceMetaInfo;
import org.example.rpcstarter.config.RegistryConfig;
import org.example.rpcstarter.config.RpcConfig;
import org.example.rpcstarter.registry.LocalRegistry;
import org.example.rpcstarter.registry.Registry;
import org.example.rpcstarter.registry.RegistryFactory;
import org.example.rpcstarter.util.ConfigUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;


/**
 * RPC服务提供者启动类，获取所有@RPCService注解修饰的类，通过注解的属性和反射机制获取要注册的服务，并注册
 */
public class RPCProvider implements BeanPostProcessor {

    /**
     * Bean 初始化后执行，注册服务
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if(beanClass.isAnnotationPresent(RPCService.class)){
            RPCService rpcService = beanClass.getAnnotation(RPCService.class);
            Class<?> interfaceClass = rpcService.interfaceClass();
            if(interfaceClass == void.class){
               interfaceClass = beanClass.getInterfaces()[0];
            }
            String serviceName = rpcService.serviceName();
            if(serviceName.isEmpty()){
                serviceName = interfaceClass.getSimpleName();
            }
            String version = rpcService.serviceVersion();
            int weight = rpcService.weight();
            //本地注册中心注册
            LocalRegistry.register(serviceName,beanClass);
            //外部注册中心注册
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(version);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            serviceMetaInfo.setWeight(weight);
            try {
                registry.addService(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException("服务注册失败："+ e);
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}

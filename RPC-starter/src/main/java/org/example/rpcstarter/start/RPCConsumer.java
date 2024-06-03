package org.example.rpcstarter.start;


import org.example.rpcstarter.annotation.RPCReference;
import org.example.rpcstarter.proxy.GoodsServiceProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import java.lang.reflect.Field;

/**
 * RPC服务消费者启动类，Bean初始化以后，获取Bean的所有字段，如果字段上有@RPCReference注解，那么为该字段生成动态代理对象并赋值
 */
public class RPCConsumer implements BeanPostProcessor {

    /**
     * Bean 初始化后执行，依赖注入
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            RPCReference rpcReference = field.getAnnotation(RPCReference.class);
            if(rpcReference != null){
                Class<?> interfaceClass = rpcReference.interfaceClass();
                if(interfaceClass == void.class){
                    interfaceClass = field.getType();
                }
                field.setAccessible(true);
                Object proxy = GoodsServiceProxyFactory.getProxy(interfaceClass);
                try {
                    field.set(bean,proxy);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("为字段注入代理失败："+ e);
                }
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}

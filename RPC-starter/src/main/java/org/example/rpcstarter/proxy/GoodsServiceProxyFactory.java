package org.example.rpcstarter.proxy;


import org.example.rpcstarter.RpcApplication;
import org.example.rpcstarter.config.RpcConfig;
import org.example.rpcstarter.constant.RPCConstants;
import org.example.rpcstarter.util.ConfigUtil;

import java.lang.reflect.Proxy;

/**
 * 代理工厂
 */
public class GoodsServiceProxyFactory {
    /**
     * 根据传入的服务类获取代理对象
     * @param serviceClass 服务类
     * @return 代理对象
     * @param <T>
     */
    public static <T> T getProxy(Class<T> serviceClass){
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        if(rpcConfig.isMock()){
            return (T) Proxy.newProxyInstance(
                    serviceClass.getClassLoader(),
                    new Class[]{serviceClass},
                    new MockServiceProxy()
            );
        }

        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new DynamicGoodsServiceProxy()
                );
    }


}

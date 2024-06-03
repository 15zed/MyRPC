package org.example.rpcstarter.registry;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地注册中心
 */
public class LocalRegistry {
    /**
     * 存放服务提供者提供的服务
     */
    private static final ConcurrentHashMap<String,Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册到本地注册中心
     * @param serviceName 服务名称
     * @param serviceImpl 服务实例
     */
    public static void register(String serviceName,Class<?> serviceImpl){
        map.put(serviceName, serviceImpl);
    }

    /**
     * 获取服务实例
     * @param serviceName 服务名称
     * @return 服务实例
     */
    public static Class<?> get(String serviceName){
        return map.get(serviceName);
    }

    /**
     * 删除服务实例
     * @param serviceName 服务名称
     */
    public static void delete(String serviceName){
        map.remove(serviceName);
    }
}

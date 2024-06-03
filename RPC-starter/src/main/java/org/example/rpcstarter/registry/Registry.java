package org.example.rpcstarter.registry;

import org.example.rpcstarter.common.ServiceMetaInfo;
import org.example.rpcstarter.config.RegistryConfig;

import java.util.List;

/**
 * 注册中心接口
 */
public interface Registry {
    /**
     * 服务初始化
     * @param registryConfig 注册中心配置对象
     */
    void init(RegistryConfig registryConfig);

    /**
     * 注册服务
     * @param serviceMetaInfos 服务信息对象
     */
    void addService(ServiceMetaInfo... serviceMetaInfos) throws Exception;

    /**
     * 注销某些服务
     * @param serviceMetaInfos 服务信息对象
     */
    void withDrawService(ServiceMetaInfo... serviceMetaInfos);

    /**
     * 服务发现
     * @param serviceKey 服务的键名
     * @return 该服务下所有的服务节点信息
     */
    List<ServiceMetaInfo> discovery(String serviceKey);

    /**
     * 服务销毁，会销毁服务中的所有节点，并关闭客户端
     */
    void destroy();

    /**
     * 心跳检测
     */
    void heartBeat();

    /**
     * 服务监听，监听某个节点
     * 服务消费端负责监听
     * @param key 保存在etcd中的key
     */
    void watch(String key);


}

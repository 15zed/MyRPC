package org.example.rpcstarter.registry;

import org.example.rpcstarter.common.ServiceMetaInfo;

import java.util.List;

/**
 * 注册中心服务的本地缓存，让消费者每次不用都从注册中心拉取可用服务
 */
public class RegistryServiceCache {
    /**
     * 服务缓存
     */
    List<ServiceMetaInfo> registerServiceCache;

    /**
     * 设置服务缓存
     * @param metaInfoList 服务信息列表
     */
    public void setCache(List<ServiceMetaInfo> metaInfoList){
        this.registerServiceCache = metaInfoList;
    }

    /**
     * 读取服务缓存
     * @return 服务集合
     */
    public List<ServiceMetaInfo> readCache(){
        return this.registerServiceCache;
    }

    /**
     * 清空缓存
     */
    public void clearCache(){
        this.registerServiceCache = null;
    }

}

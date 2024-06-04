package org.example.rpcstarter.registry;

import org.example.rpcstarter.common.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册中心服务的本地缓存，让消费者每次不用都从注册中心拉取可用服务
 */
public class RegistryServiceCache {
    /**
     * 服务缓存
     * key是服务键名称：GoodsService:1.0  value是服务节点信息
     */
    Map<String,List<ServiceMetaInfo>> registerServiceCache = new ConcurrentHashMap<>();

    /**
     * 设置服务缓存
     * @param serviceKey 服务键名
     * @param serviceMetaInfos 服务信息列表
     */
    public void setCache(String serviceKey,List<ServiceMetaInfo> serviceMetaInfos){
        this.registerServiceCache.put(serviceKey,serviceMetaInfos);
    }

    /**
     * 读取服务缓存
     * @param serviceKey 服务键名
     * @return 服务集合
     */
    public List<ServiceMetaInfo> readCache(String serviceKey){
        return this.registerServiceCache.get(serviceKey);
    }

    /**
     * 清空缓存
     */
    public void clearCache(){
        this.registerServiceCache = null;
    }
}

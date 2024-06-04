package org.example.rpcstarter.registry;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.vertx.core.impl.ConcurrentHashSet;
import lombok.SneakyThrows;
import org.example.rpcstarter.common.BusinessException;
import org.example.rpcstarter.config.RegistryConfig;
import org.example.rpcstarter.common.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NacosRegistry implements Registry {
    /**
     * 用于服务发现和注册，允许服务实例向注册中心注册自己的信息，并查询注册中心中的服务信息。
     */
    private NamingService namingService;
    /**
     * 用于配置管理，允许应用程序动态地获取配置信息，监听配置变化，并向配置中心发布、删除配置信息。
     */
    private ConfigService configService;

    /**
     * 本地注册的服务节点集合，服务节点注册到外部注册中心后，会放入本集合
     * 该集合用来续期
     */
    private Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 注册中心服务的本地缓存，让消费者每次不用都从注册中心拉取可用服务
     */
    private RegistryServiceCache serviceCache = new RegistryServiceCache();

    /**
     * 被监听的key集合，为了避免重复监听同一个key，导致某些方法被多次执行，也为了避免线程安全问题，使用ConcurrentHashSet
     */
    private Set<String> watchKeySet = new ConcurrentHashSet<>();



    @Override
    public void init(RegistryConfig registryConfig) {
        try {
            namingService = NamingFactory.createNamingService(registryConfig.getAddress());
            configService = NacosFactory.createConfigService(registryConfig.getAddress());
            heartBeat();
        } catch (NacosException e) {
            throw new BusinessException(ErrorCode.NACOS_CLIENT_INIT_ERROR, e.getMessage());
        }
    }

    @Override
    public void addService(ServiceMetaInfo... serviceMetaInfos) {
        try {
            for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfos) {
                Instance instance = new Instance();
                instance.setIp(serviceMetaInfo.getServiceHost());
                instance.setPort(serviceMetaInfo.getServicePort());
                instance.setServiceName(serviceMetaInfo.getServiceName());
                namingService.registerInstance(serviceMetaInfo.getServiceKey(), instance);
                localRegisterNodeKeySet.add(serviceMetaInfo.getServiceNodeKey());
            }
        } catch (NacosException e) {
            throw new BusinessException(ErrorCode.REGISTER_SERVICE_ERROR, e.getMessage());
        }
    }

    @Override
    public void withDrawService(ServiceMetaInfo... serviceMetaInfos) {
        try {
            for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfos) {
                namingService.deregisterInstance(serviceMetaInfo.getServiceKey(), serviceMetaInfo.getServiceHost(), serviceMetaInfo.getServicePort());
                localRegisterNodeKeySet.remove(serviceMetaInfo.getServiceNodeKey());
            }
        } catch (NacosException e) {
            throw new BusinessException(ErrorCode.UNREGISTER_SERVICE_ERROR, e.getMessage());
        }
    }



    @Override
    public List<ServiceMetaInfo> discovery(String serviceKey) {
        try {
            //先从缓存中获取
            List<ServiceMetaInfo> serviceMetaInfoList = serviceCache.readCache(serviceKey);
            if (serviceMetaInfoList != null && !serviceMetaInfoList.isEmpty()) {
                return serviceMetaInfoList;
            }
            //缓存没有
            List<Instance> instances = namingService.getAllInstances(serviceKey);
            List<ServiceMetaInfo> serviceMetaInfos = instances.stream().map(instance -> {
                ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
                serviceMetaInfo.setServiceName(instance.getServiceName());
                watch(instance.getServiceName());
                serviceMetaInfo.setServiceHost(instance.getIp());
                serviceMetaInfo.setServicePort(instance.getPort());
                return serviceMetaInfo;
            }).collect(Collectors.toList());
            serviceCache.setCache(serviceKey,serviceMetaInfos);
            return serviceMetaInfos;
        } catch (NacosException e) {
            throw new BusinessException(ErrorCode.NACOS_CLIENT_DISCOVERY_ERROR, e.getMessage());
        }
    }

    @SneakyThrows
    @Override
    public void destroy() {
        try {
            for (String nodeKey : localRegisterNodeKeySet) {
                ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.fromNodeKey(nodeKey);
                namingService.deregisterInstance(serviceMetaInfo.getServiceKey(),  serviceMetaInfo.getServiceHost(), serviceMetaInfo.getServicePort());
            }
        } catch (NacosException e) {
            throw new BusinessException(ErrorCode.NODE_OFFLINE_ERROR, e.getMessage());
        } finally {
            if (namingService != null) {
                namingService.shutDown();
            }
        }
    }

    @Override
    public void heartBeat() {
        //不需要，NamingService在注册服务的时候会是以哦那个默认的心跳机制
    }

    @Override
    public void watch(String key) {
        // Nacos 目前不直接支持按键的监听，但可以通过订阅服务实现相关功能
        boolean result = watchKeySet.add(key);
        if (result) {
            try {
                namingService.subscribe(key, event -> {
                    if(event instanceof NamingEvent){
                        NamingEvent namingEvent = (NamingEvent) event;
                        namingEvent.getInstances().forEach(instance -> {
                            if(instance.isHealthy()){
                                //服务实例被更新或者新增
                                serviceCache.clearCache();
                            }else {
                                //服务实例被删除
                                serviceCache.clearCache();
                            }
                        });
                    }
                });
            } catch (NacosException e) {
                throw new BusinessException(ErrorCode.UNKNOWN_ERROR, e.getMessage());
            }
        }
    }
}

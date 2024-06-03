package org.example.rpcstarter.registry;

import io.vertx.core.impl.ConcurrentHashSet;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.example.rpcstarter.common.BusinessException;
import org.example.rpcstarter.common.ErrorCode;
import org.example.rpcstarter.common.ServiceMetaInfo;
import org.example.rpcstarter.config.RegistryConfig;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * zookeeper注册中心实现
 */
public class ZookeeperRegistry implements Registry {
    /**
     * curator 客户端需要的
     */
    CuratorFramework client;
    /**
     * curator 客户端需要的
     */
    ServiceDiscovery<ServiceMetaInfo> serviceDiscovery;

    /**
     * 根节点
     */
    private static final String ZK_ROOT_PATH = "/rpc/zookeeper";//这里一定不要写成"/rpc/zookeeper/"，不然第69行代码会报错，因为这是zookeeper中所有服务信息的根路径，zookeeper规定必须以/开头，不能以/结尾

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

    /**
     * 服务初始化
     *
     * @param registryConfig 注册中心配置对象
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        //初始化客户端
        client = CuratorFrameworkFactory.newClient(registryConfig.getAddress(),new ExponentialBackoffRetry(Math.toIntExact(registryConfig.getTimeout()),3));
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetaInfo.class)
                .client(client)
                .basePath(ZK_ROOT_PATH)
                .serializer(new JsonInstanceSerializer<>(ServiceMetaInfo.class))
                .build();
        //启动客户端
        try {
            client.start();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.ZOOKEEPER_CLIENT_START_ERROR,"curator客户端启动失败:"+e);
        }
    }

    /**
     * 注册服务
     *
     * @param serviceMetaInfos 服务信息对象
     */
    @Override
    public void addService(ServiceMetaInfo... serviceMetaInfos) throws Exception {
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfos) {
            serviceDiscovery.registerService(buildInstance(serviceMetaInfo));
            //加入本地注册节点集合
            localRegisterNodeKeySet.add(ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey());// "/rpc/zookeeper/GoodsService:1.0/localhost:8080"
        }
    }

    /**
     * 构造ServiceInstance实例
     * @param serviceMetaInfo 服务信息对象
     * @return ServiceInstance实例
     */
    public ServiceInstance<ServiceMetaInfo>  buildInstance(ServiceMetaInfo serviceMetaInfo){
        String serviceAddress = serviceMetaInfo.getServiceHost() + ":" + serviceMetaInfo.getServicePort();
        try {
            return ServiceInstance
                    .<ServiceMetaInfo>builder()
                    .id(serviceAddress)
                    .name(serviceMetaInfo.getServiceKey())   //name + id =  GoodsService:1.0 localhost:8080
                    .address(serviceAddress)
                    .payload(serviceMetaInfo)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("ServiceInstance实例构建失败："+ e);
        }
    }

    /**
     * 注销某些服务
     *
     * @param serviceMetaInfos 服务信息对象
     */
    @Override
    public void withDrawService(ServiceMetaInfo... serviceMetaInfos) {
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfos) {
            try {
                serviceDiscovery.unregisterService(buildInstance(serviceMetaInfo));
                localRegisterNodeKeySet.remove(ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey());
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.UNREGISTER_SERVICE_ERROR,e.getMessage());
            }
        }
    }

    /**
     * 服务发现
     *
     * @param serviceKey 服务的键名
     * @return 该服务下所有的服务节点信息
     */
    @Override
    public List<ServiceMetaInfo> discovery(String serviceKey) {
        //先从缓存中获取
        List<ServiceMetaInfo> serviceMetaInfos = serviceCache.readCache();
        if (serviceMetaInfos != null && !serviceMetaInfos.isEmpty()) {
            return serviceMetaInfos;
        }
        //缓存没有,去查
        try {
            Collection<ServiceInstance<ServiceMetaInfo>> serviceInstances = serviceDiscovery.queryForInstances(serviceKey);
            List<ServiceMetaInfo> metaInfoList = serviceInstances.stream().map(ServiceInstance::getPayload).collect(Collectors.toList());
            //监听
            for (ServiceMetaInfo serviceMetaInfo : metaInfoList) {
                watch(serviceMetaInfo.getServiceNodeKey());
            }
            //写入缓存
            serviceCache.setCache(metaInfoList);
            return metaInfoList;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UNKNOWN_ERROR,e.getMessage());
        }
    }

    /**
     * 服务销毁，会销毁服务中的所有节点，并关闭客户端
     */
    @Override
    public void destroy() {
        //因为服务下线，临时节点会被自动删除，所以只需要关闭客户端就行了
        if(client != null){
            client.close();
        }
    }

    /**
     * 心跳检测
     */
    @Override
    public void heartBeat() {
        // 不需要心跳机制，建立了临时节点，如果服务器故障，则临时节点直接丢失，
        //临时节点生命周期与客户端会话周期一致。换句话说，当与ZooKeeper的会话结束（比如客户端断开连接）时，这些节点将会自动删除。
        //临时节点常用于实现服务注册机制和临时状态存储。
    }

    /**
     * 服务监听，监听某个节点
     * 服务消费端负责监听
     *
     * @param key serviceNodeKey = GoodsService:1.0/localhost:8080
     */
    @Override
    public void watch(String key) {
        String watchKey = ZK_ROOT_PATH + "/" + key;
        System.out.println("监听这个节点："+ watchKey);// "/rpc/zookeeper/GoodsService:1.0/localhost:8080
        boolean newWatch = watchKeySet.add(watchKey);
        if (newWatch) {
            CuratorCache curatorCache = CuratorCache.build(client, watchKey);
            curatorCache.start();
            curatorCache.listenable().addListener(
                    CuratorCacheListener
                            .builder()
                            .forDeletes(childData -> serviceCache.clearCache())//当该节点被删除时监听，清空服务节点缓存
                            .forChanges(((oldNode, node) -> serviceCache.clearCache()))//当节点改变时监听，清空服务节点缓存
                            .build()
            );
        }
    }
}

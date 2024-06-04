package org.example.rpcstarter.registry;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.vertx.core.impl.ConcurrentHashSet;
import org.example.rpcstarter.common.BusinessException;
import org.example.rpcstarter.common.ErrorCode;
import org.example.rpcstarter.common.ServiceMetaInfo;
import org.example.rpcstarter.config.RegistryConfig;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * etcd注册中心核心实现
 */
public class EtcdRegistry implements Registry {
    /**
     * jetcd 客户端需要的
     */
    Client client;
    /**
     * jetcd 客户端需要的
     */
    KV kvClient;

    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

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
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        heartBeat();
    }

    /**
     * 注册服务
     *
     * @param serviceMetaInfos 服务信息对象
     */
    @Override
    public void addService(ServiceMetaInfo... serviceMetaInfos) throws ExecutionException, InterruptedException {
        //获取租约客户端
        Lease leaseClient = client.getLeaseClient();
        //设置30秒的租约时间
        long id = leaseClient.grant(30L).get().getID();
        //设置放入 etcd 的key-value ，这里value是注册信息对象，要序列化
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfos) {
            String serviceNodeKey = serviceMetaInfo.getServiceNodeKey();
            ByteSequence key = ByteSequence.from((ETCD_ROOT_PATH + serviceNodeKey), StandardCharsets.UTF_8);
            ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);
            //放入etcd，并和租约时间相关联
            PutOption putOption = PutOption.builder().withLeaseId(id).build();
            kvClient.put(key, value, putOption);
            //放入本地注册节点集合
            localRegisterNodeKeySet.add(ETCD_ROOT_PATH + serviceNodeKey);// "/rpc/GoodsService:1.0/localhost:8080"
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
            String serviceNodeKey = serviceMetaInfo.getServiceNodeKey();
            String key = ETCD_ROOT_PATH + serviceNodeKey;
            kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8));
            localRegisterNodeKeySet.remove(key);
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
        List<ServiceMetaInfo> serviceMetaInfos = serviceCache.readCache(serviceKey);
        if (serviceMetaInfos != null && !serviceMetaInfos.isEmpty()) {
            return serviceMetaInfos;
        }
        //缓存没有
        //根据服务名称作为前缀，搜索服务的所有节点
        String prefix = ETCD_ROOT_PATH + serviceKey + "/";
        //构建前缀查询
        GetOption getOption = GetOption.builder().isPrefix(true).build();
        try {
            List<KeyValue> keyValueList = kvClient.get(ByteSequence.from(prefix, StandardCharsets.UTF_8), getOption).get().getKvs();
            //解析服务信息：因为value是ServiceMetaInfo对象的json格式，所以需要转换为对象
            List<ServiceMetaInfo> metaInfoList = keyValueList.stream().map(keyValue -> {
                String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                watch(key);//监听key的变化，当key被删除的时候，清空服务缓存
                String jsValue = keyValue.getValue().toString(StandardCharsets.UTF_8);
                return JSONUtil.toBean(jsValue, ServiceMetaInfo.class);
            }).collect(Collectors.toList());
            //写入服务缓存
            serviceCache.setCache(serviceKey,metaInfoList);
            return metaInfoList;
        } catch (InterruptedException | ExecutionException e) {
            throw new BusinessException(ErrorCode.ETCD_CLIENT_SEARCH_ERROR,e.getMessage());
        }
    }

    /**
     * 服务销毁，会销毁服务中的所有节点，并关闭客户端
     */
    @Override
    public void destroy() {
        //所有服务节点下线
        for (String nodeKey : localRegisterNodeKeySet) {
            try {
                long deleted = kvClient.delete(ByteSequence.from(nodeKey, StandardCharsets.UTF_8)).get().getDeleted();
                System.out.println(deleted + "个节点被删除，节点：" + nodeKey + "下线");
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.NODE_OFFLINE_ERROR,nodeKey+": "+e);
            }
        }
        //释放客户端
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }

    /**
     * 心跳检测
     */
    @Override
    public void heartBeat() {
        //设置20秒续签一次，因为我们默认设置的过期时间是30秒，续签的时间间隔小于过期时间才能保证在过期之前完成续签
        CronUtil.schedule("*/20 * * * * *", (Task) () -> {
            //遍历所有服务节点的名称
            for (String nodekey : localRegisterNodeKeySet) {
                try {
                    //根据服务节点的名称去注册中心查，只会查出一个具体的节点，比如 /rpc/GoodsService:1.0/localhost:8080
                    List<KeyValue> keyValueList = kvClient.get(ByteSequence.from(nodekey, StandardCharsets.UTF_8)).get().getKvs();
                    //如果该节点已经过期了，代表该节点没有在规定时间内续期，该节点可能已经挂了，需要重启
                    if (keyValueList.isEmpty()) {
                        continue;
                    }
                    //续期，直接重新注册一次就算是续期了
                    String jsValue = keyValueList.get(0).getValue().toString(StandardCharsets.UTF_8);
                    ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(jsValue, ServiceMetaInfo.class);
                    addService(serviceMetaInfo);
                } catch (InterruptedException | ExecutionException e) {
                    throw new BusinessException(ErrorCode.NODE_RENEWAL_ERROR,nodekey + "续期失败，" + e);
                }
            }
        });

        // 设置秒级别定时任务
        CronUtil.setMatchSecond(true);
        // 确保 CronUtil 未启动
        if (CronUtil.getScheduler().isStarted()) {
            CronUtil.stop();
        }
        CronUtil.start();
    }

    /**
     * 服务监听，监听某个节点
     * 服务消费端负责监听
     *
     * @param key 保存在etcd中的key
     */
    @Override
    public void watch(String key) {
        Watch watchClient = client.getWatchClient();
        boolean result = watchKeySet.add(key);
        if (result) {
            watchClient.watch(ByteSequence.from(key, StandardCharsets.UTF_8), watchResponse -> {
                List<WatchEvent> events = watchResponse.getEvents();
                for (WatchEvent event : events) {
                    WatchEvent.EventType eventType = event.getEventType();
                    switch (eventType) {
                        case DELETE:
                            //如果删除了某个key，那么清空服务缓存，不论该key是由于过期被被动删除，还是服务提供者下线被主动删除，都可以被监听到
                            serviceCache.clearCache();
                            break;
                        case PUT:
                        case UNRECOGNIZED:
                        default:
                            break;
                    }
                }
            });
        }
    }
}

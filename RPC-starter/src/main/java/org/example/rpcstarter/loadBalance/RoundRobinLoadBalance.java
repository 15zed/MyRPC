package org.example.rpcstarter.loadBalance;

import org.example.rpcstarter.common.BusinessException;
import org.example.rpcstarter.common.ErrorCode;
import org.example.rpcstarter.common.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡算法
 */
public class RoundRobinLoadBalance implements LoadBalancer {

    /**
     * 计数器，用来随机
     */
    AtomicInteger index = new AtomicInteger(0);

    /**
     * 选择方法
     *
     * @param requestParameters 请求参数
     * @param metaInfoList      可用服务列表
     * @return 具体某个服务
     */
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParameters, List<ServiceMetaInfo> metaInfoList) {
        if(metaInfoList.isEmpty()){
            throw new BusinessException(ErrorCode.NO_SERVICE_AVAILABLE);
        }
        if(metaInfoList.size() == 1){
            return metaInfoList.get(0);
        }
        int i = index.getAndIncrement() % (metaInfoList.size());
        return metaInfoList.get(i);
    }
}

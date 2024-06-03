package org.example.rpcstarter.loadBalance;

import org.example.rpcstarter.common.BusinessException;
import org.example.rpcstarter.common.ErrorCode;
import org.example.rpcstarter.common.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class WeightRoundRobinLoadBalance implements LoadBalancer {
    private AtomicInteger currentIndex = new AtomicInteger(0);

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
        int sumWeight = 0;
        for (ServiceMetaInfo serviceMetaInfo : metaInfoList) {
            sumWeight += serviceMetaInfo.getWeight();
        }
        int currentWeight = currentIndex.getAndIncrement() % sumWeight;
        for (ServiceMetaInfo serviceMetaInfo : metaInfoList) {
            int weight = serviceMetaInfo.getWeight();
            if(currentWeight < weight){
                return serviceMetaInfo;
            }
            currentWeight -= weight;
        }
        return null;
    }
}

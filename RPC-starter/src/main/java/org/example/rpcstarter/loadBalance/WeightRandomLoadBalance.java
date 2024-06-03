package org.example.rpcstarter.loadBalance;

import org.example.rpcstarter.common.BusinessException;
import org.example.rpcstarter.common.ErrorCode;
import org.example.rpcstarter.common.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 随机负载均衡算法
 */
public class WeightRandomLoadBalance implements LoadBalancer {
    Random random = new Random();
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
        int sumWeight = 0;
        //计算权重的和
        for (ServiceMetaInfo serviceMetaInfo : metaInfoList) {
            sumWeight += serviceMetaInfo.getWeight();
        }
        //随机生成[0,sumWeight)之间的权重
        int currentWeight = random.nextInt(sumWeight);
        //递减找到第一个小于服务节点权重的服务节点返回
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

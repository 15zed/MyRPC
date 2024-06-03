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
public class RandomLoadBalance implements LoadBalancer {
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
        return metaInfoList.get(random.nextInt(metaInfoList.size()));
    }
}

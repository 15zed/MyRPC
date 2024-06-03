package org.example.rpcstarter.loadBalance;

import org.example.rpcstarter.common.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * 负载均衡接口
 */
public interface LoadBalancer {
    /**
     * 选择方法
     * @param requestParameters 请求参数
     * @param metaInfoList 可用服务列表
     * @return 具体某个服务
     */
    public ServiceMetaInfo select(Map<String, Object> requestParameters, List<ServiceMetaInfo> metaInfoList);
}

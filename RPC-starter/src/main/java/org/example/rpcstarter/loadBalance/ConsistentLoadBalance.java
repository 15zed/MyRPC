package org.example.rpcstarter.loadBalance;

import org.example.rpcstarter.common.BusinessException;
import org.example.rpcstarter.common.ErrorCode;
import org.example.rpcstarter.common.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性hash负载均衡算法
 */
public class ConsistentLoadBalance implements LoadBalancer {
    /**
     * hash环
     * key：hash值
     * value：服务信息
     */
    TreeMap<Integer, ServiceMetaInfo> hashCircle = new TreeMap<>();
    /**
     * 虚拟节点数量
     */
    int NODE_NUM = 50;


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
        //构造哈希环
        for (ServiceMetaInfo serviceMetaInfo : metaInfoList) {
            for (int i = 0; i < NODE_NUM; i++) {
                //使用服务的地址(http://+host+port)+编号来计算hash值，同一个服务计算出多个hash值，放到hash环的不同位置
                int hashCode = doHash(serviceMetaInfo.getServiceAddress()+i);
                hashCircle.put(hashCode,serviceMetaInfo);
            }
        }
        //根据请求参数计算hash值
        int hashCode = doHash(requestParameters);
        //选择最近的大于等于该请求hash值的节点
        if(hashCircle.ceilingEntry(hashCode) != null){
            return hashCircle.ceilingEntry(hashCode).getValue();
        }else {
            //没有符合要讲求的，使用第一个节点
            return hashCircle.firstEntry().getValue();
        }
    }

    /**
     * 计算hash值
     * @param key
     * @return
     */
    public int doHash(Object key){
        if(key instanceof String){
            return key.hashCode();
        }
        Map<String, Object> requestParameters = (Map<String, Object>) key;
        String consumerIp = (String) requestParameters.get("consumerIp");
        return consumerIp.hashCode();
    }

}

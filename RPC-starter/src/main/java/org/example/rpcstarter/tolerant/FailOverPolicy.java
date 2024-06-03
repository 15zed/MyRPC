package org.example.rpcstarter.tolerant;

import org.example.rpcstarter.common.*;
import org.example.rpcstarter.proxy.DynamicGoodsServiceProxy;
import org.example.rpcstarter.serializer.Serializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 故障转移
 * 调用其他可用的服务
 */
public class FailOverPolicy implements TolerantPolicy {
    /**
     * 容错方法
     *
     * @param context 请求上下文信息
     * @param e       失败发生的异常
     * @return
     */
    @Override
    public RPCResponse doTolerance(Map<String, Object> context, Exception e) throws IOException {
        ServiceMetaInfo currentService = (ServiceMetaInfo) context.get("currentService");
        List<ServiceMetaInfo> serviceList = (List<ServiceMetaInfo>) context.get("serviceList");
        Serializer serializer = (Serializer) context.get("serializer");
        RPCRequest rpcRequest = (RPCRequest) context.get("request");


        serviceList.remove(currentService);
        Optional<ServiceMetaInfo> metaInfoOptional = serviceList.stream().findAny();
        if(metaInfoOptional.isPresent()){
            ServiceMetaInfo serviceMetaInfo = metaInfoOptional.get();
            return DynamicGoodsServiceProxy.doRequest(serviceMetaInfo,serializer,rpcRequest);
        }
        throw new BusinessException(ErrorCode.NO_SERVICE_AVAILABLE);
    }
}

package org.example.rpcstarter.common;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * 服务信息类,外部注册中心用的,注册中心的每个服务都封装为该类型对象
 */
@Data
public class ServiceMetaInfo {
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 服务版本号
     */
    private String serviceVersion = "1.0";
    /**
     * 服务主机地址
     */
    private String serviceHost;
    /**
     * 服务端口
     */
    private Integer servicePort;
    /**
     * 服务分组（扩展）
     */
    private String serviceGroup = "default";
    /**
     * 服务权重
     */
    private int weight = 1;

    public static ServiceMetaInfo fromNodeKey(String nodeKey) {
        //GoodsService:1.0/localhost:8080
        String[] split = nodeKey.split(":");
        String host = split[1].split("/")[1];
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(split[0]);
        serviceMetaInfo.setServiceHost(host);
        serviceMetaInfo.setServicePort(Integer.valueOf(split[2]));
        return serviceMetaInfo;
    }

    /**
     * 获取服务注册键名
     * @return
     */
    public String getServiceKey(){
        return String.format("%s:%s",serviceName,serviceVersion);//GoodsService:1.0
    }

    /**
     * 获取服务注册节点的键名
     * @return
     */
    public String getServiceNodeKey(){
        return String.format("%s/%s:%s",getServiceKey(),serviceHost,servicePort);//GoodsService:1.0/localhost:8080
    }

    /**
     * 获取完整的服务地址
     * @return
     */
    public String getServiceAddress(){
        if(!StrUtil.contains(serviceHost,"http")){
            return String.format("http://%s:%s",serviceHost,servicePort);
        }
        return String.format("%s:%s",serviceHost,servicePort);
    }

}

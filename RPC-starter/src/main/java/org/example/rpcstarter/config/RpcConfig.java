package org.example.rpcstarter.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.rpcstarter.constant.LoadBalanceConstants;
import org.example.rpcstarter.constant.RetryPolicyConstants;
import org.example.rpcstarter.constant.SerializerConstants;
import org.example.rpcstarter.constant.TolerantConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * RPC全局配置类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "myRPC";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;
    /**
     * 开启模拟调用
     */
    private boolean mock = false;
    /**
     * 序列化器
     */
    private String serializer = SerializerConstants.DEFAULT;
    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalanceConstants.ROUND_ROBIN;
    /**
     * 重试策略
     */
    private String retryPolicy = RetryPolicyConstants.FIXED_WAIT;
    /**
     * 容错策略
     */
    private String tolerantPolicy = TolerantConstants.FAIL_FAST;

}

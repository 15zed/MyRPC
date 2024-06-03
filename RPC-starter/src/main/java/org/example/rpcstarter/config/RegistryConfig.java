package org.example.rpcstarter.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.rpcstarter.constant.RegistryConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RPC 框架注册中心配置类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistryConfig {

    /**
     * 注册中心类别
     */
    private String registry = RegistryConstants.DEFAULT;

    /**
     * 注册中心地址,这是etcd默认的服务地址
     */
    private String address = "http://localhost:2379";

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 超时时间（单位毫秒）
     */
    private Long timeout = 10000L;
}

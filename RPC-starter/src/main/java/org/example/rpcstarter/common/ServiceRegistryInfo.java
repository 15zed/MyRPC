package org.example.rpcstarter.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册信息类，本地注册中心用的
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRegistryInfo {
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 服务实现类
     */
    private Class<?> serviceImpl;
}

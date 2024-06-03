package org.example.rpcstarter.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.rpcstarter.constant.RPCConstants;

import java.io.Serializable;

/**
 * 统一RPC请求对象
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RPCRequest implements Serializable {
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 服务的版本号
     */
    private String serviceVersion = RPCConstants.DEFAULT_SERVICE_VERSION;

    /**
     * 参数类型列表
     */
    private Class<?>[] parameterTypes;

    /**
     * 参数列表
     */
    private Object[] args;
}

package org.example.rpcstarter.tolerant;

import org.example.rpcstarter.common.RPCResponse;

import java.io.IOException;
import java.util.Map;

/**
 * 容错策略接口
 * 用来处理服务调用失败的情况
 */
public interface TolerantPolicy {
    /**
     * 容错方法
     * @param context 请求上下文信息
     * @param e 失败发生的异常
     * @return
     */
    RPCResponse doTolerance(Map<String,Object> context, Exception e) throws IOException;
}

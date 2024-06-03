package org.example.rpcstarter.tolerant;


import org.example.rpcstarter.common.RPCResponse;

import java.util.Map;

/**
 * 快速失败策略
 * 立即抛给上层
 */
public class FailFastPolicy implements TolerantPolicy {
    /**
     * 容错方法
     *
     * @param context 请求上下文信息
     * @param e       失败发生的异常
     * @return
     */
    @Override
    public RPCResponse doTolerance(Map<String, Object> context, Exception e) {
        throw new RuntimeException("服务出错："+ e);
    }
}

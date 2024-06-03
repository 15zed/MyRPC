package org.example.rpcstarter.tolerant;

import org.example.rpcstarter.common.RPCResponse;

import java.util.Map;

/**
 * 故障恢复策略
 * 在故障节点修复或恢复后，将服务或流量从备份节点或临时环境切换回到原始的主节点或系统环境中的策略。
 * 在这里可以使用降级来实现
 */
public class FailBackPolicy implements TolerantPolicy {
    /**
     * 容错方法
     *
     * @param context 请求上下文信息
     * @param e       失败发生的异常
     * @return
     */
    @Override
    public RPCResponse doTolerance(Map<String, Object> context, Exception e) {
        return new RPCResponse(null, Object.class,"默认返回",e);
    }
}

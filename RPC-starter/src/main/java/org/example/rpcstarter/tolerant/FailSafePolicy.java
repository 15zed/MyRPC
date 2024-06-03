package org.example.rpcstarter.tolerant;

import lombok.extern.slf4j.Slf4j;
import org.example.rpcstarter.common.RPCResponse;

import java.util.Map;

/**
 * 静默处理策略
 * 直接记录日志，返回正常响应，这里的日志可以使用logback，输出到外部文件保存更好
 */
@Slf4j
public class FailSafePolicy implements TolerantPolicy {
    /**
     * 容错方法
     *
     * @param context 请求上下文信息
     * @param e       失败发生的异常
     * @return
     */
    @Override
    public RPCResponse doTolerance(Map<String, Object> context, Exception e) {
        log.info("服务出错：" + e);
        return new RPCResponse();
    }
}

package org.example.rpcstarter.retry;

import com.github.rholder.retry.RetryException;
import org.example.rpcstarter.common.RPCResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * 重试策略接口
 * 用来处理服务调用失败的情况
 */
public interface RetryPolicy {
    /**
     * 重试方法
     * @param task 重试任务
     * @return 任务的执行结果
     */
     RPCResponse doRetry(Callable<RPCResponse> task) throws ExecutionException, RetryException;

}

package org.example.rpcstarter.retry;

import com.github.rholder.retry.*;
import org.example.rpcstarter.common.RPCResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 固定等待时长策略，比如每次重试等待5s
 */
public class FixedWaitPolicy implements RetryPolicy {
    /**
     * 重试方法
     *
     * @param task 重试任务
     * @return 任务的执行结果
     */
    @Override
    public RPCResponse doRetry(Callable<RPCResponse> task) throws ExecutionException, RetryException {
        //重试构造器
        Retryer<RPCResponse> retryer = RetryerBuilder.<RPCResponse>newBuilder()
                .retryIfException()
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withWaitStrategy(WaitStrategies.fixedWait(5L,TimeUnit.SECONDS))
                .build();
        //执行重试任务
        return retryer.call(task);
    }
}

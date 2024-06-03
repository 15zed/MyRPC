package org.example.rpcstarter.retry;

import com.github.rholder.retry.*;
import org.example.rpcstarter.common.RPCResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 递增等待时长策略，指定初始等待值，然后重试间隔随次数等差递增，比如依次等待10s、30s、60s（递增值为10）
 */
public class IncrementingWaitPolicy implements RetryPolicy {
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
                .withWaitStrategy(WaitStrategies.incrementingWait(5,TimeUnit.SECONDS,5,TimeUnit.SECONDS))
                .build();
        //执行重试任务
        return retryer.call(task);
    }
}

package org.example.rpcstarter.retry;

import com.github.rholder.retry.*;
import org.example.rpcstarter.common.RPCResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 指数等待时长策略，指定初始值，然后每次重试间隔乘2（即间隔为2的幂次方），如依次等待 2s、6s、14s。
 * 可以设置最大等待时长，达到最大值后每次重试将等待最大时长。
 */
public class ExponentialWaitPolicy implements RetryPolicy {
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
                .withWaitStrategy(WaitStrategies.exponentialWait(20,TimeUnit.SECONDS))
                .build();
        //执行重试任务
        return retryer.call(task);
    }
}

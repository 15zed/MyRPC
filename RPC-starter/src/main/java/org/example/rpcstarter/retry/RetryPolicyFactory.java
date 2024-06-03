package org.example.rpcstarter.retry;


import org.example.rpcstarter.spi.SpiLoader;

/**
 * 重试策略工厂
 */
public class RetryPolicyFactory{

    private RetryPolicyFactory() {}

    static {
        SpiLoader.load(RetryPolicy.class);
    }

    public static RetryPolicy getInstance(String retryPolicyName){
        return SpiLoader.getInstance(RetryPolicy.class,retryPolicyName);
    }
}

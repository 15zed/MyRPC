package org.example.rpcstarter.constant;

/**
 * 重试策略常量
 */
public class RetryPolicyConstants {
    /**
     * 固定时间间隔等待策略
     */
    public static final String FIXED_WAIT = "fixedWait";
    /**
     * 等差递增时间等待策略
     */
    public static final String INCREMENT_WAIT = "incrementWait";
    /**
     * 指数递增时间等待策略
     */
    public static final String EXPONENTIAL_WAIT = "exponentialWait";
}

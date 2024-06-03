package org.example.rpcstarter.constant;

/**
 * 负载均衡常量
 */
public class LoadBalanceConstants {
    /**
     * 轮询
     */
    public static final String ROUND_ROBIN = "roundRobin";
    /**
     * 随机
     */
    public static final String RANDOM = "random";
    /**
     * 一致性
     */
    public static final String CONSISTENT = "consistent";
    /**
     * 加权随机
     */
    public static final String WEIGHT_RANDOM = "weightRandom";
    /**
     * 加权轮询
     */
    public static final String WEIGHT_ROUND_ROBIN = "weightRoundRobin";
}

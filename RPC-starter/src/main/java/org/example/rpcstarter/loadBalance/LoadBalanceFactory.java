package org.example.rpcstarter.loadBalance;

import org.example.rpcstarter.spi.SpiLoader;

/**
 * 负载均衡工厂
 */
public class LoadBalanceFactory {
    private LoadBalanceFactory() {}

    static {
        SpiLoader.load(LoadBalancer.class);
    }

    public static LoadBalancer getInstance(String loadBalanceName){
        return SpiLoader.getInstance(LoadBalancer.class,loadBalanceName);
    }
}

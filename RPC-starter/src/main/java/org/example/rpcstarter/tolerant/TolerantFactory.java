package org.example.rpcstarter.tolerant;

import org.example.rpcstarter.spi.SpiLoader;

/**
 * 容错策略工厂
 */
public class TolerantFactory {
    private TolerantFactory() {}

    static {
        SpiLoader.load(TolerantPolicy.class);
    }

    public static TolerantPolicy getInstance(String tolerantPolicyName){
        return SpiLoader.getInstance(TolerantPolicy.class,tolerantPolicyName);
    }
}

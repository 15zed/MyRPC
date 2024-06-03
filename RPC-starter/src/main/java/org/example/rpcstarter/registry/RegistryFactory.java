package org.example.rpcstarter.registry;

import org.example.rpcstarter.spi.SpiLoader;

/**
 * 注册中心工厂
 */
public class RegistryFactory{
    private RegistryFactory() {
    }

    static {
        SpiLoader.load(Registry.class);
    }

    public static Registry getInstance(String registryName){
        return SpiLoader.getInstance(Registry.class,registryName);
    }
}

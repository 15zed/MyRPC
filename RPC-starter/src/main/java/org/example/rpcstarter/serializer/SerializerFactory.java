package org.example.rpcstarter.serializer;

import org.example.rpcstarter.spi.SpiLoader;

/**
 * 序列化器工厂，用来获取指定的序列化器，序列化器可以复用，没必要每次都新创建一个，所以使用单例模式
 * 使用单例模式 + 工厂模式
 */
public class SerializerFactory {

    private SerializerFactory() {
    }


    static {
        SpiLoader.load(Serializer.class);
    }

    public static Serializer getInstance(String serializerName){
        return SpiLoader.getInstance(Serializer.class,serializerName);
    }

}

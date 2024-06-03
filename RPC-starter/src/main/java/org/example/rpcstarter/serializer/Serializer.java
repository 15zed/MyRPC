package org.example.rpcstarter.serializer;

import java.io.IOException;

/**
 *
 */
public interface Serializer {
    /**
     * 序列化
     * @param object 对象
     * @return  字节数组
     * @param <T>
     */
    <T> byte[] serialize(T object) throws IOException;

    /**
     * 反序列化
     * @param bytes 字节数组
     * @param aclass 类对象，用来声明要将字节数组反序列化后的类型
     * @return 类型对象
     */
     <T>  T deserialize(byte[] bytes, Class<T> aclass) throws IOException;
}

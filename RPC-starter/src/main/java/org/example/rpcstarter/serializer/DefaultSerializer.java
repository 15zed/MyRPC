package org.example.rpcstarter.serializer;

import org.example.rpcstarter.common.BusinessException;
import org.example.rpcstarter.common.ErrorCode;

import java.io.*;

/**
 *
 */
public class DefaultSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T object)  {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);){
            outputStream.writeObject(object);
            outputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SERIALIZE_ERROR,e.getMessage());
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> aclass) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream)){
            Object object = inputStream.readObject();
            return (T) object;
        } catch (IOException | ClassNotFoundException e) {
            throw new BusinessException(ErrorCode.DESERIALIZE_ERROR,e.getMessage());
        }
    }
}

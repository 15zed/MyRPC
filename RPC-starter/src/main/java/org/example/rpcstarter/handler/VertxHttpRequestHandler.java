package org.example.rpcstarter.handler;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.rpcstarter.RpcApplication;
import org.example.rpcstarter.common.RPCRequest;
import org.example.rpcstarter.common.RPCResponse;
import org.example.rpcstarter.config.RpcConfig;
import org.example.rpcstarter.registry.LocalRegistry;
import org.example.rpcstarter.serializer.Serializer;
import org.example.rpcstarter.serializer.SerializerFactory;
import org.example.rpcstarter.util.ConfigUtil;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Vertx实现的请求处理器,请求处理器负责接收请求,反射调用服务实现类
 */
@Slf4j
public class VertxHttpRequestHandler implements Handler<HttpServerRequest> {
    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        //指定序列化器
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
//        DefaultSerializer defaultSerializer = new DefaultSerializer();
        System.out.println("拦截到请求："+httpServerRequest.method()+" "+httpServerRequest.uri());
        //处理请求
        httpServerRequest.bodyHandler(requestBody -> {
            byte[] bytes = requestBody.getBytes();
            RPCRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(bytes, RPCRequest.class);
            } catch (IOException e) {
                log.error("请求反序列化失败："+ e.getCause());
            }
            Optional<RPCRequest> optional = Optional.ofNullable(rpcRequest);
            //构造响应
            RPCResponse rpcResponse = new RPCResponse();
            //如果请求为空，直接返回
            if (!optional.isPresent()) {
                rpcResponse.setMessage("请求为空");
                returnResponse(httpServerRequest,rpcResponse,serializer);
                return;
            }
            String serviceName = optional.get().getServiceName();
            String methodName = optional.get().getMethodName();
            Class<?>[] parameterTypes = optional.get().getParameterTypes();
            Object[] args = optional.get().getArgs();
           //反射调用服务实现类
            try {
                Class<?> service = LocalRegistry.get(serviceName);
                Method method = service.getMethod(methodName, parameterTypes);
                Object result = method.invoke(method.getDeclaringClass().newInstance(), args);
                //封装调用结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            //返回响应
            returnResponse(httpServerRequest,rpcResponse,serializer);
        });
    }

    /***
     * 响应封装
     * @param httpServerRequest 请求
     * @param rpcResponse 统一响应对象
     * @param serializer 序列化器
     */
    void returnResponse(HttpServerRequest httpServerRequest, RPCResponse rpcResponse, Serializer serializer){
        HttpServerResponse response = httpServerRequest.response();
        response.putHeader("content-type", "application/json");
        byte[] bytes = new byte[0];
        try {
            bytes = serializer.serialize(rpcResponse);
            response.end(Buffer.buffer(bytes));
        } catch (IOException e) {
            log.error("序列化响应失败："+e.getCause());
            response.end(Buffer.buffer(bytes));
        }
    }
}

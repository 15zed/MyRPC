package org.example.rpcstarter.proxy;

import com.github.rholder.retry.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.example.rpcstarter.RpcApplication;
import org.example.rpcstarter.common.*;
import org.example.rpcstarter.config.RegistryConfig;
import org.example.rpcstarter.config.RpcConfig;
import org.example.rpcstarter.constant.RPCConstants;
import org.example.rpcstarter.loadBalance.LoadBalanceFactory;
import org.example.rpcstarter.loadBalance.LoadBalancer;
import org.example.rpcstarter.registry.Registry;
import org.example.rpcstarter.registry.RegistryFactory;
import org.example.rpcstarter.retry.RetryPolicy;
import org.example.rpcstarter.retry.RetryPolicyFactory;
import org.example.rpcstarter.serializer.Serializer;
import org.example.rpcstarter.serializer.SerializerFactory;
import org.example.rpcstarter.tolerant.TolerantFactory;
import org.example.rpcstarter.tolerant.TolerantPolicy;
import org.example.rpcstarter.util.ConfigUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 服务接口的动态代理
 */
public class DynamicGoodsServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws IOException {
        //指定序列化器
//        DefaultSerializer defaultSerializer = new DefaultSerializer();
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        //构造RPC请求对象
        RPCRequest rpcRequest = new RPCRequest();
        rpcRequest.setServiceName(method.getDeclaringClass().getSimpleName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setArgs(args);
        //从注册中心获取服务地址，注册中心的discovery方法会先从服务缓存中获取服务地址
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(method.getDeclaringClass().getSimpleName());
        serviceMetaInfo.setServiceVersion(RPCConstants.DEFAULT_SERVICE_VERSION);
        List<ServiceMetaInfo> serviceList = registry.discovery(serviceMetaInfo.getServiceKey());
        if (serviceList.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_SERVICE_AVAILABLE);
        }
        //选择第一个服务调用，后面可以根据算法定制规则
//            ServiceMetaInfo serviceMetaInfo1 = serviceList.get(0);
        LoadBalancer balancer = LoadBalanceFactory.getInstance(rpcConfig.getLoadBalancer());
        HashMap<String, Object> map = new HashMap<>();
        map.put("methodName", rpcRequest.getMethodName());
        map.put("consumerIp",getConsumerIpAddress());
        ServiceMetaInfo serviceMetaInfo1 = balancer.select(map, serviceList);
        System.out.println("当前选择的服务是：" + serviceMetaInfo1.toString());
        RPCResponse rpcResponse;
        //重试机制
        RetryPolicy retryPolicy = RetryPolicyFactory.getInstance(rpcConfig.getRetryPolicy());
        try {
            rpcResponse = retryPolicy.doRetry(() -> doRequest(serviceMetaInfo1, serializer, rpcRequest));

        } catch (ExecutionException | RetryException e) {
            //这里可以进行其他操作，比如降级调用其他接口，或者发布告警信息给开发人员
//            throw new RuntimeException("调用服务失败："+ e);

            //容错机制
            TolerantPolicy tolerantPolicy = TolerantFactory.getInstance(rpcConfig.getTolerantPolicy());
            HashMap<String, Object> context = new HashMap<>();
            context.put("request",rpcRequest.getMethodName());
            context.put("currentService",serviceMetaInfo1);
            context.put("serviceList",serviceList);
            context.put("serializer",serializer);
            rpcResponse = tolerantPolicy.doTolerance(context,e);
        }
        return rpcResponse.getData();
    }

    /**
     * 发请求
     * @param serviceMetaInfo 服务
     * @param serializer 序列化器
     * @param rpcRequest RPC请求对象
     * @return RPC响应对象
     * @throws IOException
     */
    public static RPCResponse doRequest(ServiceMetaInfo serviceMetaInfo, Serializer serializer, RPCRequest rpcRequest) throws IOException {
        RPCResponse rpcResponse = new RPCResponse();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            //序列化
            byte[] bytes = serializer.serialize(rpcRequest);
            HttpPost httpPost = new HttpPost(serviceMetaInfo.getServiceAddress());
            ByteArrayEntity requestEntity = new ByteArrayEntity(bytes);
            httpPost.setEntity(requestEntity);
            //处理响应
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity responseEntity = httpResponse.getEntity();
            if (responseEntity != null) {
                byte[] bytes1 = EntityUtils.toByteArray(responseEntity);
                rpcResponse = serializer.deserialize(bytes1, RPCResponse.class);
                return rpcResponse;
            }
        }
        return rpcResponse;
    }

    /**
     * 获取服务消费者的ip地址
     * @return
     */
    private static String getConsumerIpAddress() {
         List<String> IP_SERVICE_URLS = Arrays.asList(
                "https://api.ipify.org",
                "https://ipv4.icanhazip.com",
                "https://checkip.amazonaws.com"
        );

        Callable<String> task = new Callable<String>() {
            int count = 0;
            @Override
            public String call() throws Exception {
                String currentUrl = IP_SERVICE_URLS.get(count % IP_SERVICE_URLS.size());
                count++;
                URL url = new URL(currentUrl);
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                return br.readLine();
            }
        };

        //重试构造器
        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
                .retryIfException()
                .withStopStrategy(StopStrategies.stopAfterAttempt(9))
                .withWaitStrategy(WaitStrategies.fixedWait(5L,TimeUnit.SECONDS))
                .build();
        //执行重试任务
        try {
            return retryer.call(task);
        } catch (ExecutionException | RetryException e) {
            return "127.0.0.1";
        }
    }

    public static void main(String[] args) {
        System.out.println(DynamicGoodsServiceProxy.getConsumerIpAddress());
    }
}

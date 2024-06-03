package org.example.rpcstarter;


import org.example.rpcstarter.config.RegistryConfig;
import org.example.rpcstarter.config.RpcConfig;
import org.example.rpcstarter.constant.RPCConstants;
import org.example.rpcstarter.registry.Registry;
import org.example.rpcstarter.registry.RegistryFactory;
import org.example.rpcstarter.util.ConfigUtil;

/**
 * RPC框架应用类，主要存放RPC框架的全局变量
 * 这个RpcConfig对象是全局唯一的，使用单例模式
 */
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;

    /**
     * 自定义初始化，可以传入自定义配置
     * @param newRpcConfig
     */
    public static void init(RpcConfig newRpcConfig){
        rpcConfig = newRpcConfig;
        System.out.println("自定义配置加载："+rpcConfig);
        // 注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        // 创建并注册 Shutdown Hook，JVM 退出时执行操作
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown Hook 被触发，开始注销所有服务...");
            registry.destroy();
            System.out.println("所有服务已注销。");
        }));
    }

    /***
     * 框架初始化
     */
    public static void init(){
        RpcConfig newRpcConfig;
        try {
            if(rpcConfig == null){
                newRpcConfig = ConfigUtil.loadConfiguration(RpcConfig.class, RPCConstants.DEFAULT_CONFIG_PREFIX);
                rpcConfig = newRpcConfig;
                // 注册中心初始化
                RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
                Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
                registry.init(registryConfig);
                // 创建并注册 Shutdown Hook，JVM 退出时执行操作
//                Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("Shutdown Hook 被触发，开始注销所有服务...");
                    registry.destroy();
                    System.out.println("所有服务已注销。");
                }));
            }
        }catch (Exception e){
            // 配置加载失败，使用默认值
            System.out.println("配置加载失败，使用默认初始化");
            newRpcConfig = new RpcConfig();
            rpcConfig = newRpcConfig;
            // 注册中心初始化
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            registry.init(registryConfig);
            // 创建并注册 Shutdown Hook，JVM 退出时执行操作
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutdown Hook 被触发，开始注销所有服务...");
                registry.destroy();
                System.out.println("所有服务已注销。");
            }));
        }
    }

    /**
     * 获取全局配置对象,双检索单例模式
     * @return
     */
    public static RpcConfig getRpcConfig(){
        if (rpcConfig == null){
            synchronized (RpcApplication.class){
                if(rpcConfig == null){
                    init();
                }
            }
        }
        return rpcConfig;
    }

}

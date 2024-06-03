package org.example.rpcstarter.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.rpcstarter.serializer.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI 加载器
 * 自定义实现，支持键值对映射
 */
@Slf4j
public class SpiLoader {

    /**
     * 存储已加载的类：接口名 =>（key => 实现类）
     * key是配置文件中的key，代表序列化器的名称
     *
     * org.example.rpcstarter.serializer.Serializer={
     *    jdk=class org.example.serializer.DefaultSerializer,
     *    hessian=class org.example.serializer.HessianSerializer,
     *    json=class org.example.serializer.JsonSerializer,
     *    kryo=class org.example.serializer.KryoSerializer
     * }
     */
    private static final Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 对象实例缓存（避免重复 new），实现类名称 => 对象实例，单例模式
     *
     * {org.example.serializer.DefaultSerializer=org.example.serializer.DefaultSerializer@3bbc39f8,
     * org.example.serializer.HessianSerializer=org.example.serializer.HessianSerializer@4ae3c1cd,
     * org.example.serializer.JsonSerializer=org.example.serializer.JsonSerializer@5ea434c8,
     * org.example.serializer.KryoSerializer=org.example.serializer.KryoSerializer@29f69090}
     */
    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * SPI 目录
     */
    private static final String RPC_SPI_DIR = "META-INF/rpc/";


    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_SPI_DIR};

    /**
     * 动态加载的类列表
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    /**
     * 加载所有类型
     */
    public static void loadAll() {
        log.info("加载所有 SPI");
        for (Class<?> aClass : LOAD_CLASS_LIST) {
            load(aClass);
        }
    }

    /**
     * 获取某个接口的实例
     *
     * @param tClass 接口类型
     * @param key 序列化器名称
     * @param <T>
     * @return 序列化器对象
     */
    public static <T> T getInstance(Class<?> tClass, String key) {
        String tClassName = tClass.getName();
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);
        if (keyClassMap == null) {
            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型", tClassName));
        }
        if (!keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoader 的 %s 不存在 key=%s 的类型", tClassName, key));
        }
        // 获取到要加载的实现类型
        Class<?> implClass = keyClassMap.get(key);
        // 从实例缓存中加载指定类型的实例，反射
        String implClassName = implClass.getName();
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                String errorMsg = String.format("%s 类实例化失败", implClassName);
                throw new RuntimeException(errorMsg, e);
            }
        }
        return (T) instanceCache.get(implClassName);
    }

    /**
     * 加载某个类型
     *
     * @param loadClass 类型
     * @throws IOException
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("加载类型为 {} 的 SPI", loadClass.getName());
        // 扫描路径，用户自定义的 SPI 优先级高于系统 SPI
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        for (String scanDir : SCAN_DIRS) {
            //getResources方法获取指定路径下的资源列表
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            // 读取每个资源文件
            for (URL resource : resources) {
                try {
                    //openStream()方法获取输入流来读取文件流
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] strArray = line.split("=");
                        if (strArray.length > 1) {
                            String key = strArray[0];
                            String className = strArray[1];
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    log.error("spi resource load error", e);
                }
            }
        }
        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }

    public static void main(String[] args) {
        loadAll();
        System.out.println(loaderMap);
        Serializer serializer = getInstance(Serializer.class, "json");
        System.out.println(serializer);
        System.out.println(instanceCache);
    }

}

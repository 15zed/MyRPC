package org.example.rpcstarter.util;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.example.rpcstarter.config.RpcConfig;


import java.io.InputStream;
import java.util.Map;

/**
 * 配置工具类
 */

public class ConfigUtil {


    /**
     * 在配置文件中使用前缀有几个重要的理由，特别是当配置文件用于存储多个不同组件或模块的配置信息时。这种做法增强了配置的可读性、组织性和模块化。下面详细解释为什么要在这种情况下设置前缀参数：
     *
     * ### 1. **增强可读性和维护性**
     *
     * 通过为不同的模块或组件配置设置不同的前缀，可以清晰地区分它们，使配置文件更易于理解和维护。例如，`database.url`和`server.port`很明显属于不同的配置范畴，前者用于数据库连接配置，后者用于服务器配置。
     *
     * ### 2. **避免键名冲突**
     *
     * 在较大的应用程序中，不同模块可能需要各自的配置参数，这些参数可能具有相似或相同的名称但用于不同的目的。使用前缀可以防止这类键名冲突。例如，`email.timeout`和`database.timeout`用前缀区分了电子邮件服务和数据库服务的超时配置。
     *
     * ### 3. **增加配置的模块化**
     *
     * 前缀支持将配置按模块组织，使得各个模块的配置更加独立。这对于模块化开发和配置的重用非常有帮助。如果在未来需要迁移或重构某个模块，相关的配置信息可以更容易地识别和迁移。
     *
     * ### 4. **简化配置信息的映射**
     *
     * 在映射配置信息到对象属性时，可以利用前缀来过滤只属于特定模块的配置。这样，同一个配置文件可以用于不同的模块或组件，而通过传入不同的前缀，`loadConfig`方法能够创建出各自配置好的模块实例。这种方式提高了代码的复用性，并减少了配置的冗余。
     *
     * ### 5. **支持分层配置**
     *
     * 在一些情况下，应用程序可能采用基于环境的配置（如开发环境、测试环境、生产环境），同时每个环境中又有多个模块需要配置。使用前缀可以很自然地将这些配置进行分层，先按环境分，再按模块分，这种结构可以让配置管理更加清晰。
     *
     * ### 结论
     *
     * 使用前缀参数允许`loadConfig`方法在同一个配置文件中为不同的应用组件提取和应用专属的配置参数。这为构建可维护、模块化和灵活的配置系统提供了基础。
     */


    /**
     * 根据配置文件构建配置类对象
     * @param tClass 对象的类型
     * @param prefix 前缀
     * @return 配置对象的实例
     * @param <T>
     */
    public static <T> T loadConfiguration(Class<T> tClass,String prefix){
        return loadConfiguration(tClass,prefix,"");
    }

    /**
     * 可以传入环境参数
     * @param tClass 对象的类型
     * @param prefix
     * @param environment
     * @return
     * @param <T>
     */
    public static <T> T loadConfiguration(Class<T> tClass,String prefix,String environment){
        StringBuilder stringBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            stringBuilder.append("-").append(environment);
        }
        stringBuilder.append(".yml");

        InputStream inputStream = ConfigUtil.class.getClassLoader().getResourceAsStream(stringBuilder.toString());
        if (inputStream == null) {
            throw new RuntimeException("YAML configuration file not found: " + stringBuilder.toString());
        }

        try {

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Map<String, Object> yamlMap = mapper.readValue(inputStream, Map.class);

            Map<String, Object> configMap = getNestedMap(yamlMap, prefix);
            return mapper.convertValue(configMap, tClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }

    }

    private static Map<String, Object> getNestedMap(Map<String, Object> map, String prefix) {
        String[] keys = prefix.split("\\.");
        Map<String, Object> nestedMap = map;
        for (String key : keys) {
            nestedMap = (Map<String, Object>) nestedMap.get(key);
            if (nestedMap == null) {
                throw new RuntimeException("Invalid prefix: " + prefix);
            }
        }
        return nestedMap;
    }

    public static void main(String[] args) {
        RpcConfig rpcConfig = loadConfiguration(RpcConfig.class, "rpc"); // Use the appropriate environment
        System.out.println(rpcConfig);
    }
}

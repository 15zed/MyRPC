package org.example.rpcstarter.common;

import lombok.Getter;

/**
 * 错误码
 */
@Getter
public enum ErrorCode {
    ok(200,"ok",""),

    NO_SERVICE_AVAILABLE(501,"没有服务可用",""),

    GET_CONSUMER_IP_ERROR(502,"获取客户端ip失败",""),

    MAGIC_MISMATCH(503,"magic不匹配",""),

    ABSENT_PROTOCOL(504,"序列化协议不存在",""),

    ABSENT_SERIALIZATION_TYPE(505,"序列化类型不存在",""),

    UNSUPPORTED_MESSAGE_TYPE(506,"不支持此消息的类型",""),

    ETCD_CLIENT_SEARCH_ERROR(507,"",""),

    NODE_OFFLINE_ERROR(508,"节点下线失败",""),

    NODE_RENEWAL_ERROR(509,"节点续期失败",""),

    ZOOKEEPER_CLIENT_START_ERROR(510,"",""),

    UNREGISTER_SERVICE_ERROR(511,"服务注销失败",""),

    UNKNOWN_ERROR(512,"未知错误",""),

    SERIALIZE_ERROR(513,"序列化失败",""),

    DESERIALIZE_ERROR(514,"反序列化失败",""),

    REGISTER_SERVICE_ERROR(515,"服务注册失败",""),

    NACOS_CLIENT_INIT_ERROR(516,"nacos客户端异常","" ),

    NACOS_CLIENT_DISCOVERY_ERROR(517,"" ,"" );

    private int code;

    private String message;

    private String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
}

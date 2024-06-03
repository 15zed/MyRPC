package org.example.rpcstarter.server.http;

/**
 * 服务器接口
 */
public interface HttpServer {
    /**
     * 启动服务器
     * @param port 端口
     */
    void start(int port);
}

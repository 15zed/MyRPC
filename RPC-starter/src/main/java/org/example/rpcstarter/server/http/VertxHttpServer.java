package org.example.rpcstarter.server.http;


import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.example.rpcstarter.handler.VertxHttpRequestHandler;

/**
 * Vertx 实现的HTTP服务器
 */
@Slf4j
public class VertxHttpServer implements HttpServer {
    @Override
    public void start(int port) {
        //创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();
        //创建 HTTP 服务器
        io.vertx.core.http.HttpServer httpServer = vertx.createHttpServer();
        //处理请求
        httpServer.requestHandler(new VertxHttpRequestHandler());
        //启动 HTTP 服务器并监听指定端口
        httpServer.listen(port,httpServerAsyncResult -> {
            if(httpServerAsyncResult.succeeded()){
                System.out.println("成功启动，监听端口："+port);
            }else {
                System.out.println("启动失败，原因："+httpServerAsyncResult.cause());
            }
        });
    }
}

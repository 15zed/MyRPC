package org.example.springbootrpcprovider;

import org.example.rpcstarter.annotation.EnableRPC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRPC
public class SpringbootRpcProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootRpcProviderApplication.class, args);
    }

}

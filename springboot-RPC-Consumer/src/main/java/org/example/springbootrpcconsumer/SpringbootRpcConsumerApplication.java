package org.example.springbootrpcconsumer;

import org.example.model.Goods;
import org.example.rpcstarter.annotation.EnableRPC;
import org.example.service.GoodsService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.util.Optional;

@SpringBootApplication
@EnableRPC(openServer = false)
public class SpringbootRpcConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootRpcConsumerApplication.class, args);

    }

}

package org.example.springbootrpcconsumer;

import org.example.model.Goods;
import org.example.rpcstarter.annotation.RPCReference;
import org.example.service.GoodsService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 *
 */
@Service
public class ServiceImpl {
    @RPCReference()
    GoodsService goodsService;

    public void test(){
        Goods goods = new Goods("农夫山泉", "食品", 2.00, LocalDate.of(2024, 4, 15), "180天", "500瓶");
        System.out.println(goodsService.get(goods));
    }
}

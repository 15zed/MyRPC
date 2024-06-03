package org.example.springbootrpcprovider;

import org.example.model.Goods;
import org.example.rpcstarter.annotation.RPCService;
import org.example.service.GoodsService;
import org.springframework.stereotype.Service;

/**
 * 商品服务的实现类
 */
@RPCService()
@Service
public class GoodsServiceImpl implements GoodsService {
    /**
     * 获取商品信息
     * @param goods
     * @return
     */
    @Override
    public Goods get(Goods goods) {
        System.out.println("商品服务的实现类,商品信息："+goods.toString());
        return goods;
    }

    @Override
    public int getId(Goods goods) {
        return 1;
    }


}

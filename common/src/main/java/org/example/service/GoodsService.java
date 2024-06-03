package org.example.service;

import org.example.model.Goods;

/**
 * 公共的接口
 * 商品服务接口
 */
public interface GoodsService {
    /**
     * 获取商品
     * @param goods
     * @return
     */
    Goods get(Goods goods);

    int getId(Goods goods);
}

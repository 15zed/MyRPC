package org.example.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 公共的实体类
 * 商品类
 */
public class Goods implements Serializable {
    public Goods(String name, String type, Double price, LocalDate bornDate, String effectiveTime, String stock) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.bornDate = bornDate;
        this.effectiveTime = effectiveTime;
        this.stock = stock;
    }

    public Goods() {
    }

    /**
     * 商品id
     */
    private Integer id;
    /**
     * 名称
     */
    private String name;
    /**
     * 种类
     */
    private String type;
    /**
     * 价格
     */
    private Double price;
    /**
     * 生产日期
     */
    private LocalDate bornDate;
    /**
     * 保质期
     */
    private String effectiveTime;
    /**
     * 库存
     */
    private String stock;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDate getBornDate() {
        return bornDate;
    }

    public void setBornDate(LocalDate bornDate) {
        this.bornDate = bornDate;
    }

    public String getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(String effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "Goods{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", price=" + price +
                ", bornDate=" + bornDate +
                ", effectiveTime='" + effectiveTime + '\'' +
                ", stock='" + stock + '\'' +
                '}';
    }
}

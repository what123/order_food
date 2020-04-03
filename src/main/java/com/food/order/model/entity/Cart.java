package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 购物车
 */
@Data
@Entity
public class Cart extends BaseEntity {
    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "table_id", nullable = true)
    private Tables tables = null; //餐桌

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store = null; //归属的分店

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "consumer_id", nullable = true)
    private Consumer consumer =null; //归属的客户

    @OneToOne(targetEntity = Goods.class)
    @JoinColumn(name = "goods_id",referencedColumnName = "id")
    private Goods goods;//菜品

    private Integer num = 1;//数量

    @Column(columnDefinition = "text")
    private String note;//备注介绍

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "share_cart_id", nullable = true)
    private ShareCart shareCart = null; //共享



}

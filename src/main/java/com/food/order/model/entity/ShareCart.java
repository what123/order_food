package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 共享购物车
 */
@Data
@Entity
public class ShareCart extends BaseEntity {
    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "table_id", nullable = true)
    private Tables tables = null; //餐桌

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store = null; //归属的分店

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "consumer_id", nullable = true)
    private Consumer consumer =null; //归属的客户

    private String sharePassword;//共享密码

    @ManyToMany
    @JoinTable(name="share_cart_with_consumer",	//其他客户
            //用于指定本表在中间表的字段名称，以及中间表依赖的是本表的哪个字段
            joinColumns= {@JoinColumn(name="share_cart_id",referencedColumnName="id")},
            //用于指定对方表在中间表的字段名称，以及中间表依赖的是它的哪个字段
            inverseJoinColumns= {@JoinColumn(name="consumer_id",referencedColumnName="id")})
    private List<Consumer> consumerList = new ArrayList<>();


}

package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 店员
 */
@Data
@Entity
public class StoreUser extends BaseEntity {
    private String number;//店员编号
    private String name;//店员名
    private Integer sex;

    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date birthday = null;
    private String telephones;//电话

    @OneToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private User user;

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; //归属的店家

    @Column(columnDefinition = "text")
    private String note;//备注介绍

    @Transient
    private boolean isBindOauth = false;

}

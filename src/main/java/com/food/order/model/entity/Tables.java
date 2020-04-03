package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 餐桌
 */
@Data
@Entity
public class Tables extends BaseEntity {

    private String number;//编号，拼桌时，多个桌子的编号用#号隔开
    private String name;
    @Column(columnDefinition = "text")
    private String note;//备注介绍
    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; //归属的店家
    private int status = 1;//1：空闲，2使用中
    private boolean isAssemble = false;//是否拼桌，如果是拼桌，则这个桌子是零时的，用完就删除并使所有桌子的状态为未使用，拼桌时桌子使用状态也全部修改为使用中

    private int minPeopleCount;//最少坐几人
    private int maxPeopleCount;//最多坐几人

    private String uuid;


    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_user_id", nullable = true)
    private StoreUser storeUser = null; //负责的店员

    @Transient
    private String qrPic;


}

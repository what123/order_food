package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * 充值项
 */
@Data
@Entity
public class ChargeItem extends BaseEntity {
    private Integer chargePrice;//充值金额
    private Integer totalPrice;//得到金额

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "main_store_id", nullable = true)
    private MainStore mainStore = null;

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = true)
    private Store store = null; //归属的分店


    private String tag;
    private int status = 1;//1未上线，2已上线

}

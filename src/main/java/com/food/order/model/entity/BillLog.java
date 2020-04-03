package com.food.order.model.entity;

import com.food.order.model.entity.BaseEntity;
import com.food.order.model.entity.Consumer;
import lombok.Data;

import javax.persistence.*;

/**
 * 账单日志
 */
@Data
@Entity
public class BillLog extends BaseEntity {
    @Column(columnDefinition = "text")
    private String note;
    private Double balance = 0d;//钱包余额
    private Double money;//支付或者收入
    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "consumer_id", nullable = true)
    private Consumer consumer;

    @JoinColumn(name = "store_id", nullable = true)
    private Store store;
}

package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 分店内所有操作的日志
 */
@Data
@Entity
public class StoreLog extends BaseEntity {
    @Column(columnDefinition = "text")
    private String note;
    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_user_id", nullable = true)
    private StoreUser storeUser = null;

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = true)
    private Store store;
}

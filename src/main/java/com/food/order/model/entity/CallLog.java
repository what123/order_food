package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * 呼叫日志
 */
@Data
@Entity
public class CallLog extends BaseEntity {
    private String content;//服务内容,如加水

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_user_id", nullable = true)
    private StoreUser storeUser = null; //服务人

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store = null; //归属的分店

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "table_id", nullable = false)
    private Tables tables = null; //桌号

    private int status = 1;//1未处理，2已处理

}

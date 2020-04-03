package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 快速服务标签
 */
@Data
@Entity
public class CallTag extends BaseEntity {
    private String content;//服务内容,如加水
    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = true)
    private Store store = null; //归属的分店

    private int status = 1;//状态1.下架，2上架

}

package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 抽奖游戏
 */
@Data
@Entity
public class GamesPrize extends BaseEntity {
    @Column(columnDefinition = "text")
    private String title;
    @Column(columnDefinition = "text")
    private String rule;//规则
    private Integer dayCount = null;//每天抽奖次数,null不限制
    private Integer count = null;//总抽奖次数，null不限制
    private String lotteryPath="";//抽奖游戏类型,实际与页面路径配置
    
    private Double balance = 0d;//钱包余额
    private Double money;//支付或者收入
    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "consumer_id", nullable = true)
    private Consumer consumer;

    @JoinColumn(name = "store_id", nullable = true)
    private Store store;
}

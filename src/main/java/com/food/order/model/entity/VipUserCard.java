package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 用户的会员卡
 */
@Data
@Entity
public class VipUserCard extends BaseEntity{

    private String pluginTag;//
    private String pluginUUID;//

//    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
//    @JoinColumn(name = "plugin_id", nullable = false)
//    private Plugins plugins; //logo

    @OneToOne(targetEntity = Consumer.class)
    @JoinColumn(name = "consumer_id",referencedColumnName = "id")
    private Consumer consumer; //归属的客户

    private Integer bonus = 0;//积分

    private Integer balance = 0;//余额

    private Integer grade = 1;//等级

    private String cardNo;//会员编号




}

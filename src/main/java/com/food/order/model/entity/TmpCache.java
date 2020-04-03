package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 临时缓存，最好用redis代替
 * Created by Administrator on 2018/4/2.
 */
@Data
@Entity
public class TmpCache extends BaseEntity{
    @Column(columnDefinition = "text")
    private String cKey;
    @Column(columnDefinition = "text")
    private String cValue;
    private Integer expireTime = 3600*1000*2;//有效期2小时
    protected Long endTime;


}

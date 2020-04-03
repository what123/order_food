package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 店家
 */
@Data
@Entity
public class MainStore extends BaseEntity {
    private String name;//店家名
    private String province;//省
    private String city;//市
    private String area;//区
    private String address;//地址
    private String latlng;//经纬度坐标
    private String telephons;//电话，有多个时用英文分号隔开
    private Integer limitStore = 0;//分店数量限制,0表示无限制
    private String storePrefix = "";//分店管理员帐号

    @OneToOne(targetEntity = User.class)

    @JoinColumn(name = "super_user_id",referencedColumnName = "id")
    private User user;//超管

    @Column(columnDefinition = "text")
    private String note;//备注介绍

    private int vip = 0;//0,免费用户,1收费用户，2私有云用户

    private Long pageViewId = 0l;//客户点餐主题页面

}

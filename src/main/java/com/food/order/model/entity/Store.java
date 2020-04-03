package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 店铺
 */
@Data
@Entity
public class Store extends BaseEntity {
    private String name;//店名
    private String province;//省
    private String city;//市
    private String area;//区
    private String address;//地址
    private String latlng;//经纬度坐标
    private String telephons;//电话，有多个时用英文分号隔开
    private String startTime;//营业开始时间
    private String endTime;//营业结束时间
    private int configType;//1.使用店家的支付和授权登录统一配置，2使用分店自己的支付和授权登录配置
    private int payConfig = 1;//1.预付费制，2后付费制

    private Long pageViewId = 0l;//客户点餐主题页面

    @OneToOne(targetEntity = User.class)
    @JoinColumn(name = "super_user_id",referencedColumnName = "id")
    private User user;//超管

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "main_store_id", nullable = false)
    private MainStore mainStore; //归属的店家

    @Column(columnDefinition = "text")
    private String note;//备注介绍


    private int status = 1;//1正常营业，2.已下线

}

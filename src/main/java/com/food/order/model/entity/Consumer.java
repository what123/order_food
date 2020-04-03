package com.food.order.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.food.order.utils.utils.MD5Util;
import lombok.Data;

import javax.persistence.*;
import java.security.NoSuchAlgorithmException;

/**
 * 消费者(顾客)
 * Created by Administrator on 2018/4/2.
 */
@Data
@Entity
public class Consumer extends BaseEntity{
    @JsonIgnore
    private String wxOpenId;
    private String telephone;
    private String name;
    private String nickName;
    private String photo;
    private String accessToken = null;

    @OneToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private User user;//

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "main_store_id", nullable = false)
    private MainStore mainStore = null; //归属的店家

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store = null; //归属的分店


    @Column(columnDefinition = "text")
    private String photoUrl;

    private int sex = 1;

    private String province;

    private String city;

    private String country;

    @Transient
    private boolean isBlack = false;//加入黑名单


}

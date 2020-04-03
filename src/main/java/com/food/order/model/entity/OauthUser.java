package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 第三方授权信息
 */
@Data
@Entity
public class OauthUser extends BaseEntity {

    private String openid;//openid
    //private String oauth_user_id;//第三方的id
    private String unionid = null;//关联id
    private String tag;//授权插件类
    private int type = OauthUserTypeEnum.CONSUMER.getIndex();//1

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Transient
    private String name;
    @Transient
    private String nickname;
    @Transient
    private String photoUrl;
    @Transient
    private int sex;
    @Transient
    private String province;
    @Transient
    private String city;
    @Transient
    private String country;

}

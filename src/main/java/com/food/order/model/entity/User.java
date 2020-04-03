package com.food.order.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.food.order.utils.utils.MD5Util;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
 * 用户表
 */
@Data
@Entity
public class User extends BaseEntity {

    private String account;

    @JsonIgnore
    private String password;

    private String accessToken;

    @Column(columnDefinition = "text")
    private String note;//备注介绍

    private UploadFile photo;//头像

    private int isFreeze = 0;//0:正常，1冻结

    private int belong = 5;//归属 1.平台超管，2店家超管，3店家管理，4店员，5顾客


    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastLoginTime = null;//最后登录时间

    public String getPwd(String srcpwd){
        try {
            return MD5Util.MD5Encode(srcpwd + account);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}

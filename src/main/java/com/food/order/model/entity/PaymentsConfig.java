package com.food.order.model.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 支付配置
 */
@Data
@Entity
public class PaymentsConfig extends BaseEntity {

    private String showName;//显示名称
    private String icon;//图片
    private String name;
    private String tag;
    private int payOrCollect = 1;//1.付款或2收款

    @JsonIgnore
    @Column(columnDefinition = "text")
    private String params;//json保存的参数

    @Transient
    private Plugins plugins = null;//

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "main_store_id", nullable = true)
    private MainStore mainStore = null; //归属的店家

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = true)
    private Store store = null; //归属的分店

    private int status = 1;//状态1.下架，2上架

    @Transient
    private OauthUser oauthUser = null;

    @Transient
    private Map<String,Object> paramsMap;//参数

    public Map<String, Object> getParamsMap() {
        return JSON.parseObject(params);
    }

    public boolean isSystem = false;//系统的支付
}

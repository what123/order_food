package com.food.order.model.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Map;

/**
 * 第三方授权信息
 */
@Data
@Entity
public class OauthConfig extends BaseEntity {

    private String name;

    private String tag;

    @JsonIgnore
    private String paramsStr;//配置参数

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
    private Map<String,Object> paramsMap;//参数

    public Map<String, Object> getParamsMap() {
        return JSON.parseObject(paramsStr);
    }
}

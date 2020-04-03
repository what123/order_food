package com.food.order.model.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Map;

/**
 * 抽奖游戏
 */
@Data
@Entity
public class GamesConfig extends BaseEntity {
    @Column(columnDefinition = "text")
    private String title;
    @Column(columnDefinition = "text")
    private String rule;//规则

    private String name;
    private String tag;

    @JsonIgnore
    @Column(columnDefinition = "text")
    private String params;//json保存的参数

    @Transient
    private Map<String,Object> paramsMap;//参数

    public Map<String, Object> getParamsMap() {
        return JSON.parseObject(params);
    }

    @JoinColumn(name = "store_id", nullable = true)
    private Store store;

    @Transient
    private Plugins plugins = null;//

    private int status = 1;//状态1.下架，2上架
}

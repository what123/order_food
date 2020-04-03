package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 * 设置
 */
@Data
@Entity
public class UserConfigs extends BaseEntity {


    private Integer valueKey;//对应settingTypeEnum 的index
    private String valueStr;
//
    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = true)
    private Store store;

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "main_store_id", nullable = true)
    private MainStore mainStore;


    @Transient
    private String name;//对应settingTypeEnum 的 name
    @Transient
    private Integer type;//对应settingTypeEnum 的 type
    @Transient
    private String note;

    @Transient
    private List<UserConfigParams> settingParams;

}

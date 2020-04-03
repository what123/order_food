package com.food.order.model.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 打印机
 */
@Data
@Entity
public class Printer extends BaseEntity{

    private String name;

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store = null; //归属的分店

    @JsonIgnore
    private String apiTag;

    private String pluginUUID;

    @JsonIgnore
    private String apiConfigParams;//接口配置

    @Transient
    private Map<String,Object> paramsMap;//参数

    @Transient
    private String origin_id;

    public Map<String, Object> getParamsMap() {
        return JSON.parseObject(apiConfigParams);
    }

    private int status = 1;//1未启用，2启用

    @ManyToMany
    @JoinTable(name="printer_data_model_with_data",	//用来指定中间表的名称
            //用于指定本表在中间表的字段名称，以及中间表依赖的是本表的哪个字段
            joinColumns= {@JoinColumn(name="printer_id",referencedColumnName="id")},
            //用于指定对方表在中间表的字段名称，以及中间表依赖的是它的哪个字段
            inverseJoinColumns= {@JoinColumn(name="printer_data_id",referencedColumnName="id")})
    private List<PrinterDataModel> printerDataModels = new ArrayList<>();

    @Transient
    private String printData;//打印内容


}

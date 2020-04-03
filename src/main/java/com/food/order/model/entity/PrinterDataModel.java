package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 打印数据配置
 */
@Data
@Entity
public class PrinterDataModel extends BaseEntity{

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = true)
    private Store store = null; //归属的分店

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "main_store_id", nullable = true)
    private MainStore mainStore = null; //归属的分店

    private String name;//模板名字
    private int type;//1.下单打印，2.换桌打印，3.改单打印（增减菜单），4.支付打印

    @Column(columnDefinition = "text")
    private String printData;//打印内容

//    @ManyToMany
//    @JoinTable(name="printer_with_data",	//用来指定中间表的名称
//            //用于指定本表在中间表的字段名称，以及中间表依赖的是本表的哪个字段
//            joinColumns= {@JoinColumn(name="printer_data_id",referencedColumnName="id")},
//            //用于指定对方表在中间表的字段名称，以及中间表依赖的是它的哪个字段
//            inverseJoinColumns= {@JoinColumn(name="printer_id",referencedColumnName="id")})
//    private List<Printer> printers = new ArrayList<>();


}

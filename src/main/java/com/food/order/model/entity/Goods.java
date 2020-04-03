package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜品
 */
@Data
@Entity
public class Goods extends BaseEntity {

    private String number;//编号，分店id_xxxxx

    private String name;

    private Integer marker_price;//市场价,单位分

    private Integer sell_price;//售价，单位分

    private Integer addBonus = 0;//增加的积分


    @Column(columnDefinition = "text")
    private String note;//备注介绍

    @ManyToMany
    @JoinTable(name="goods_files",	//用来指定中间表的名称
            //用于指定本表在中间表的字段名称，以及中间表依赖的是本表的哪个字段
            joinColumns= {@JoinColumn(name="goods_id",referencedColumnName="id")},
            //用于指定对方表在中间表的字段名称，以及中间表依赖的是它的哪个字段
            inverseJoinColumns= {@JoinColumn(name="upload_file_id",referencedColumnName="id")})
    private List<UploadFile> pics = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "main_store_id", nullable = true)
    private MainStore mainStore = null; //归属的店家

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = true)
    private Store store = null; //归属的分店

    @ManyToMany
    @JoinTable(name="goods_with_tags",	//用来指定中间表的名称
            //用于指定本表在中间表的字段名称，以及中间表依赖的是本表的哪个字段
            joinColumns= {@JoinColumn(name="goods_id",referencedColumnName="id")},
            //用于指定对方表在中间表的字段名称，以及中间表依赖的是它的哪个字段
            inverseJoinColumns= {@JoinColumn(name="tag_id",referencedColumnName="id")})
    private List<GoodsTag> goodsTags = new ArrayList<>();

    @ManyToMany
    @JoinTable(name="goods_with_printer",	//用来指定中间表的名称
            //用于指定本表在中间表的字段名称，以及中间表依赖的是本表的哪个字段
            joinColumns= {@JoinColumn(name="goods_id",referencedColumnName="id")},
            //用于指定对方表在中间表的字段名称，以及中间表依赖的是它的哪个字段
            inverseJoinColumns= {@JoinColumn(name="printer_id",referencedColumnName="id")})
    private List<Printer> printers = new ArrayList<>();

    private int kitchenPrinter = 1;//是否开启在厨房打印,0不打印，1.打印

    private int status = 1;//状态1.下架，2上架

    @Transient
    private int cart_num = 0;//购物车中的数量

    private int outsideStatus = 1;//外部显示，1显示，2只内部显示（如茶水位）
}

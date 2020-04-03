package com.food.order.model.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.domain.Sort;

import javax.persistence.*;
import java.util.Date;

/**
 * 订单中的菜品
 */
@Data
@Entity
public class OrderGoods extends BaseEntity {

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "order_id", nullable = false)
    //@NotFound(action= NotFoundAction.IGNORE)
    private Orders orders;

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = true)
    private Store store = null; //归属的分店

    @JsonIgnore
    @Column(columnDefinition = "text")
    private String goodsStr;//菜品json数据保存
    private String goodsName;//菜品名字
    private Long goodsId;//菜品id

    @Transient
    private Goods goods;

    private int count;//数量

    @Column(columnDefinition = "text")
    private String note;//备注介绍

    private int makingCount=0;//制作中数量

    private int makedCount=0;//制作完成数量

    private int overCount=0;//上菜完成数量

    private Integer totalPrice;//价格

    private Integer realPrice;//实收价格

    private Integer refundPrice = 0;//退款金额

    private int payStatus = 1;//1.正常，2.已退款

    private int status = 1; //1.待制作，2.制作中，3.待上菜，4完成上菜

    public Goods getGoods() {
        return JSON.parseObject(goodsStr,Goods.class);
    }

    public void setStatus(int status) {
        if(makingCount == 0 || count > makingCount){//待制作的
            status = 1;
        }else if(makingCount == count && makingCount > makedCount){//制作中
            status = 2;
        }else if(makingCount == makedCount && makedCount > overCount){//待上菜
            status = 3;
        }else{//已上菜
            status = 4;
        }
        this.status = status;
    }

    @Override
    public String toString() {
        return "OrderGoods{" +
                "orders=" + orders +
                ", store=" + store +
                ", goodsStr='" + goodsStr + '\'' +
                ", goodsName='" + goodsName + '\'' +
                ", goodsId=" + goodsId +
                ", goods=" + goods +
                ", count=" + count +
                ", note='" + note + '\'' +
                ", makingCount=" + makingCount +
                ", makedCount=" + makedCount +
                ", overCount=" + overCount +
                ", status=" + status +
                '}';
    }
}

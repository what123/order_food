package com.food.order.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 订单
 */
@Data
@Entity
public class Orders extends BaseEntity {
    private String orderNo;//订单号
    private String outOrderNo;//外部订单号
    private String outOrderNo2;//外部订单号2，payjs中还有微信订单号
    private int payStatus = 1;//1.未支付，2.已支付，3部分退款，4全部退款

    @Transient
    private String payStatusStr;

    @Temporal(value = TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date payTime = null;//支付时间

    private String payTypeStr;


    private Long paymentId;//支付类型id

    @Column(columnDefinition = "text")
    private String payResult;//支付接口返回的信息
    @Column(columnDefinition = "text")
    private String payCallBackResult;//支付接口返回的信息
    @Column(columnDefinition = "text")
    private String refundCallBackResult;//支付接口返回的信息

    private Integer totalPrice;//总价格
    private Integer realPrice;//实际支付
    private Integer refundPrice = 0;//退款金额

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "created_user_id", nullable = false)
    private User createdUser;//创建订单者ID

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "pay_user_id", nullable = true)
    private User payUser = null;//支付者ID

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = true)
    private Store store; //归属的分店



    @Column(columnDefinition = "text")
    private String note;//备注介绍

    private String tableNumber = null; //餐桌编号

    private int applyStatus = 2;//1.待确认，2已确认（后付费制的订单，需要店员确认，防止乱刷单），3.拒绝
    @Transient
    private String applyStatusStr;


    private int peopleNum;//就餐人数

    public String getPayStatusStr() {
        if(payStatus == 1){
            payStatusStr = "未支付";
        }else if(payStatus == 2){
            payStatusStr = "已支付";
        }else if(payStatus == 3){
            payStatusStr = "部分退款";
        }else if(payStatus == 4){
            payStatusStr = "已全部退款";
        }

        return payStatusStr;
    }


    public String getApplyStatusStr() {
        return applyStatus == 1?"待确认":"已确认";
    }

    private int type = 1;//1菜品订单，2插件购买订单

    //插件订单参数
    private String pluginsTag;

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "main_store_id", nullable = true)
    private MainStore mainStore = null; //归属的分店

    @Column(columnDefinition = "text")
    private String refundNote;//退款备注
}

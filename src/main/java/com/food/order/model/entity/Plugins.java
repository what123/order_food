package com.food.order.model.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

/**
 * 插件与用户的关系
 */
@Data
@Entity
public class Plugins extends BaseEntity{
    private String name;//插件名字
    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = true)
    private Store store; //归属的分店

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "main_store_id", nullable = true)
    private MainStore mainStore; //归属的总店,总店购买后，自动分配到子店

    @Column(columnDefinition = "text")
    private String note;//备注介绍

    @JsonIgnore
    private String pluginsClassPath;//类路径

    private String picPath;//图片

    private String uuid;

    private int type;

    private boolean isTest = false;//试用

    @Temporal(value = TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date startTime = new Date();//启用朝着

    @Temporal(value = TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date endTime = new Date(); //停用时间


    @Transient
    private Integer price;//对应当前自己的价格

    @Transient
    private Integer vip0Price;
    @Transient
    private Integer vip1Price;
    @Transient
    private Integer vip2Price;



    @Transient
    private int day;

    @Transient
    private int status = 1;//1.未购买，2.未开始，3已过期，4正常

    @Transient
    private String statusStr = "未购买";

    public int getStatus() {
        if(id == null || id == 0){
            status = 1;
        }else if(startTime.getTime() > System.currentTimeMillis()){
            status = 2;
        }else if(endTime.getTime() < System.currentTimeMillis()){
            status = 3;
        }else{
            status = 4;
        }
        return status;
    }

    public String getStatusStr() {
        if(id == null || id == 0){
            statusStr = "未购买";
        }else if(startTime.getTime() > System.currentTimeMillis()){
            statusStr = "开始:"+startTime;
        }else if(endTime.getTime() < System.currentTimeMillis()){
            statusStr = "已于"+endTime+"过期";
        }else{
            statusStr = "正常";
        }
        return statusStr;
    }


    @JsonIgnore
    @Column(columnDefinition = "text")
    private String paramsStr;//配置参数
    @Transient
    private Map<String,Object> paramsMap;//参数

    public Map<String, Object> getParamsMap() {
        return JSON.parseObject(paramsStr);
    }

    private boolean isUsed = false;

    @Override
    public String toString() {
        return "Plugins{" +
                "name='" + name + '\'' +
                ", store=" + store +
                ", mainStore=" + mainStore +
                ", note='" + note + '\'' +
                ", pluginsClassPath='" + pluginsClassPath + '\'' +
                ", picPath='" + picPath + '\'' +
                ", uuid='" + uuid + '\'' +
                ", type=" + type +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", price=" + price +
                ", vip0Price=" + vip0Price +
                ", vip1Price=" + vip1Price +
                ", vip2Price=" + vip2Price +
                ", day=" + day +
                ", status=" + status +
                ", statusStr='" + statusStr + '\'' +
                '}';
    }
}

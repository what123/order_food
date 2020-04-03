package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 打印日志
 */
@Data
@Entity
public class PrinterLogs extends BaseEntity{

    private String name;

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store = null; //归属的分店

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "printer_id", nullable = false)
    private Printer printer = null;

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "printer_data_model_id", nullable = false)
    private PrinterDataModel printerDataModel = null;

    @ManyToOne(cascade = CascadeType.REFRESH,optional=true)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orders = null;

    @Column(columnDefinition = "text")
    private String printData;//打印内容

    private String out_order_id;

    private int status = 1;//1未推送，2已推送，3.已推送成功，4推送失败

    @Column(columnDefinition = "text")
    private String printerApiResult;//打印平台返回的结果

}

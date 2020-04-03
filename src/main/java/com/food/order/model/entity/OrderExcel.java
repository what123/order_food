package com.food.order.model.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

/**
 * 导出订单
 */
@Data
public class OrderExcel extends BaseRowModel {
    @ExcelProperty(value = "订单号",index = 0)
    private String order_no;
    @ExcelProperty(value = "总金额(￥)",index = 1)
    private String total_price;
    @ExcelProperty(value = "实收金额(￥)",index = 2)
    private String real_price;
    @ExcelProperty(value = "退款金额(￥)",index = 3)
    private String refund_price;
    @ExcelProperty(value = "下单时间",index = 4)
    private String created_time;
    @ExcelProperty(value = "支付时间",index = 5)
    private String pay_time;
    @ExcelProperty(value = "支付方式",index = 6)
    private String payType;
    @ExcelProperty(value = "分店",index = 7)
    private String storeName;
}

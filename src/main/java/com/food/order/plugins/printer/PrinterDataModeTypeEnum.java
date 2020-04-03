package com.food.order.plugins.printer;

public enum PrinterDataModeTypeEnum {

    ORDER("下单打印", 1), PAY_ORDER("支付订单打印", 2),MAKED("厨房出餐打印(完成菜品制作)",3) ,ORDER_GOOD("厨房菜品队列打印",4),OTHER("其他打印",5);
    // 成员变量
    private String name;
    private int index;
    // 构造方法
    private PrinterDataModeTypeEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }
    // 普通方法
    public static String getName(int index) {
        for (PrinterDataModeTypeEnum c : PrinterDataModeTypeEnum.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }
    // get set 方法
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
}

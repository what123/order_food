package com.food.order.model.entity;

public enum OrderTypeEnum {

    GOODS_ORDERS("菜品订单", 1),PLUGINS_ORDERS("插件购买订单", 2),CHARGE_ORDERS("余额充值订单", 3);
    // 成员变量
    private String name;
    private int index;
    // 构造方法
    private OrderTypeEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }
    // 普通方法
    public static String getName(int index) {
        for (OrderTypeEnum c : OrderTypeEnum.values()) {
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

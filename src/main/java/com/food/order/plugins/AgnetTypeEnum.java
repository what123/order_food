package com.food.order.plugins;

public enum AgnetTypeEnum {
    ////1微信，2支付宝,3.后台手动支付,4.payjs
    PC_WEB("电脑web端", 1),PC_APP("电脑APP端", 2), MOBILE_WEB("手机普通浏览器", 3),MOBILE_APP("手机APP", 4), WX_WEB("微信浏览器", 5), WX_APP("微信小程序", 6);
    // 成员变量
    private String name;
    private int index;
    // 构造方法
    private AgnetTypeEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }
    // 普通方法
    public static String getName(int index) {
        for (AgnetTypeEnum c : AgnetTypeEnum.values()) {
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

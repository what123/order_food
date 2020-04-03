package com.food.order.plugins;

public enum PluginsTypeEnum {
    //1.微信公众号登录,2.微信扫码登录,3.payjs登录
    OAUTH_PLUGINS("授权插件", 1), PAY_PLUGINS("支付插件", 2), PAGE_VIEW_PLUGINS("主题页面插件", 3), PRINTER_PLUGINS("打印机插件", 4), ORDER_FOOD_PLUGINS("点餐高级服务", 5), GAME_PLUGINS("小游戏", 6),MARKETING_PLUGINS("营销", 6);
    // 成员变量
    private String name;
    private int index;
    // 构造方法
    private PluginsTypeEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }
    // 普通方法
    public static String getName(int index) {
        for (PluginsTypeEnum c : PluginsTypeEnum.values()) {
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

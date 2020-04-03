package com.food.order.websocket;

public enum WebsocketTypeEnum {
    //1.微信公众号登录,2.微信扫码登录,3.payjs登录
    SOCKET_SUCCESS("websocket连接成功", 1),NEW_ORDER("新订单", 2), PAY_ORDER("订单支付", 3), CALL_SERVICE("呼叫服务", 4), SHARE_CART_SERVICE("共享购物车", 5);
    // 成员变量
    private String name;
    private int index;
    // 构造方法
    private WebsocketTypeEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }
    // 普通方法
    public static String getName(int index) {
        for (WebsocketTypeEnum c : WebsocketTypeEnum.values()) {
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

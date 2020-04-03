package com.food.order.model.entity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置
 */
public enum UserConfigTypeEnum {


    ORDERS_GOODS_TYPE("厨房接单模式",1,"",1,"[{\"key\":\"1\",\"name\":\"电脑模式\",\"value\":\"1\",},{\"key\":\"2\",\"name\":\"打印机模式\",\"value\":\"2\",}]"),
    ORDERS_SURE_TYPE("确认订单",2,"",1,"[{\"key\":\"1\",\"name\":\"手动\",\"value\":\"1\",},{\"key\":\"2\",\"name\":\"自动\",\"value\":\"2\",}]");

    // ORDERS_OUT_NO("订单牌号模式",2,"",1,"[{\"key\":\"1\",\"name\":\"自动号牌\",\"value\":\"1\",},{\"key\":\"2\",\"name\":\"手动号牌\",\"value\":\"2\",}]");
    // 成员变量
    private String name;
    private int index;
    private int type;//1.开关模式，2下拉模式，3输入值模式
    private List<UserConfigParams> settingParams;
    private String note;
    // 构造方法

    List<UserConfigParams> s = new ArrayList<>();

    UserConfigTypeEnum(String name, int index, String note, int type, String paramsJson) {
        this.name = name;
        this.index = index;
        this.note = note;
        this.type = type;
        List<UserConfigParams> settingParams = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(paramsJson);
        int len = jsonArray.length();
        for(int i = 0 ; i < len ; i ++){
            JSONObject itemJson = jsonArray.getJSONObject(i);
            UserConfigParams settingParam = new UserConfigParams();
            settingParam.setKey(itemJson.getString("key"));
            settingParam.setName(itemJson.getString("name"));
            settingParam.setValue(itemJson.getString("value"));
            settingParams.add(settingParam);
        }
        this.settingParams = settingParams;
    }

    // 普通方法
    public static String getName(int index) {
        for (UserConfigTypeEnum c : UserConfigTypeEnum.values()) {
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<UserConfigParams> getSettingParams() {
        return settingParams;
    }

    public void setSettingParams(List<UserConfigParams> settingParams) {
        this.settingParams = settingParams;
    }
}

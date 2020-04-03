package com.food.order.plugins;

import org.json.JSONObject;

public class BasePlugins {

    //获取参数
    protected String getParam(String key, String paramsJson){
        JSONObject jsonObject = new JSONObject(paramsJson);
        return jsonObject.getJSONObject(key).getString("value");
    }

}

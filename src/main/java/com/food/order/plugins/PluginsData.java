package com.food.order.plugins;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PluginsData {
    private int code = 200;//100：错误，200：正常
    private String msg;
    private Map<String,Object> data = new HashMap<>();
}

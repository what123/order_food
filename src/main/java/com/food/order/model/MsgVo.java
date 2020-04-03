package com.food.order.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/3.
 */
@Data
public class MsgVo {
    private int code = 0;
    private String msg = "";
    private Map<String,Object> data = new HashMap<String,Object>();

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}

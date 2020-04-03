package com.food.order.websocket;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class WebSocketVo {
    private int type;
    private String clientId;
    private String des;
    private Map<String,Object> params = new HashMap<>();
}

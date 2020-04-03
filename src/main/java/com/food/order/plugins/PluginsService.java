package com.food.order.plugins;


import java.util.Map;

/**
 * 插件接口
 */
public interface PluginsService {
    public String getPluginsUUID();
    public String getPluginsTag();
    public int getPluginsType();
    public Integer getVip0Price();
    public Integer getVip1Price();
    public Integer getVip2Price();
    public int getExpiryDay();
    public int getFreeExpiryDay();
    public String getName();
    public String getPicPath();
    public String getNote();
    public Map<String, String> getParamsConfig();
}

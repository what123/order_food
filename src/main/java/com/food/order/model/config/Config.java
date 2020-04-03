package com.food.order.model.config;

import javax.persistence.Transient;

public class Config {

    public static String host = null;


    public static long timeStamp = 0;//时间戳

    public static long endTimeStamp = 0;//结束时间

    public static long expireDay = 0;//有效天数，0表示永久
}

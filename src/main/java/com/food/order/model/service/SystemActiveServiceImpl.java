package com.food.order.model.service;

import com.food.order.model.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SystemActiveServiceImpl {


    @Value("${spring.profiles.active}")
    private  String active;


    public void getActiveInfo(){
        long day = 0;
        Long startTimeStamp = null;
        Config.host = "http://test123.fangwei6.com";
        Config.expireDay = 10000;
        Config.timeStamp = System.currentTimeMillis();
        Config.endTimeStamp = Config.timeStamp + (1000*60*60*24*day);
    }
}

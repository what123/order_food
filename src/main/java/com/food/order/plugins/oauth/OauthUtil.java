package com.food.order.plugins.oauth;

import com.food.order.model.entity.OauthConfig;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.pay.PayTarget;
import com.food.order.plugins.pay.PaymentService;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class OauthUtil {
    public List<OauthService> getOauthClass(String tag){
        List<OauthService> oauthServices = new ArrayList<>();
        //入参 要扫描的包名
        Reflections f = new Reflections("com.food.order.plugins.oauth");
        //入参 目标注解类
        Set<Class<?>> clazzes = f.getTypesAnnotatedWith(OauthTarget.class);
        Iterator<Class<?>> it = clazzes.iterator();
        while (it.hasNext()) {
            Class<?> clazz = it.next();
            try {
                PluginsService pluginsService = (PluginsService) clazz.newInstance();
                if(tag == null || pluginsService.getPluginsTag().equals(tag)){
                    OauthService oauthService = (OauthService) clazz.newInstance();
                    oauthServices.add(oauthService);
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return oauthServices;
    }
    public OauthService getOauthClass(OauthConfig oauthConfig,String host){
        //入参 要扫描的包名
        Reflections f = new Reflections("com.food.order.plugins.oauth");
        //入参 目标注解类
        Set<Class<?>> clazzes = f.getTypesAnnotatedWith(OauthTarget.class);
        Iterator<Class<?>> it = clazzes.iterator();
        while (it.hasNext()) {
            Class<?> clazz = it.next();
            try {
                PluginsService pluginsService = (PluginsService) clazz.newInstance();
                if(oauthConfig.getTag().equals(pluginsService.getPluginsTag())){
                    OauthService oauthService = (OauthService) clazz.newInstance();
                    oauthService.setParams(oauthConfig,host);
                    return oauthService;
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

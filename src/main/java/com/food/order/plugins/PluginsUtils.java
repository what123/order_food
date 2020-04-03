package com.food.order.plugins;


import com.food.order.plugins.oauth.OauthTarget;
import com.food.order.plugins.pageView.PageViewTarget;
import com.food.order.plugins.pay.PayTarget;
import com.food.order.plugins.printer.PrinterTarget;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PluginsUtils {
    public List<PluginsService> getPluginsClass(Class clazz){
        List<PluginsService> pluginsServices = new ArrayList<>();
        getPluginsServiceByPackage(pluginsServices, clazz);
        return pluginsServices;
    }
    public List<PluginsService> getPluginsClass(){
        List<PluginsService> pluginsServices = new ArrayList<>();
        getPluginsServiceByPackage(pluginsServices, OauthTarget.class);
        getPluginsServiceByPackage(pluginsServices, PayTarget.class);
        getPluginsServiceByPackage(pluginsServices, PrinterTarget.class);
        getPluginsServiceByPackage(pluginsServices, PageViewTarget.class);
        return pluginsServices;
    }

    private void getPluginsServiceByPackage(List<PluginsService> pluginsServices, Class clazz1){
        Reflections f = new Reflections("com.food.order.plugins");
        //入参 目标注解类
        Set<Class<?>> clazzes = f.getTypesAnnotatedWith(clazz1);
        Iterator<Class<?>> it = clazzes.iterator();
        while (it.hasNext()) {
            Class<?> clazz = it.next();
            try {
                PluginsService pluginsService = (PluginsService) clazz.newInstance();
                pluginsServices.add(pluginsService);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.food.order.plugins.pageView;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PageViewUtil {
    public List<PageViewService> getPageViewClass(String viewPath){
        List<PageViewService> pageClass = new ArrayList<>();
        //入参 要扫描的包名
        Reflections f = new Reflections("com.food.order.plugins.pageView");
        //入参 目标注解类
        Set<Class<?>> clazzes = f.getTypesAnnotatedWith(PageViewTarget.class);
        Iterator<Class<?>> it = clazzes.iterator();
        while (it.hasNext()) {
            Class<?> clazz = it.next();
            try {
                PageViewService pageViewService = (PageViewService) clazz.newInstance();
                if(viewPath == null || pageViewService.getViewPath().equals(viewPath)) {
                    pageClass.add(pageViewService);
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return pageClass;
    }


}

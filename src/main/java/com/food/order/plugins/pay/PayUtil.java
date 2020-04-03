package com.food.order.plugins.pay;

import com.food.order.model.entity.Plugins;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.oauth.wechat.Wechat;
import com.food.order.plugins.pay.wxpay.WxJsapi;
import org.reflections.Reflections;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PayUtil {
    public List<PaymentService> getPaymentClass(String tag){
        List<PaymentService> payClass = new ArrayList<>();
        //入参 要扫描的包名
        Reflections f = new Reflections("com.food.order.plugins.pay");
        //入参 目标注解类
        Set<Class<?>> clazzes = f.getTypesAnnotatedWith(PayTarget.class);
        Iterator<Class<?>> it = clazzes.iterator();
        while (it.hasNext()) {
            Class<?> clazz = it.next();
            try {
                PluginsService pluginsService = (PluginsService)clazz.newInstance();
                if(tag == null || tag.equals(pluginsService.getPluginsTag())){
                    PaymentService paymentService = (PaymentService) clazz.newInstance();
                    payClass.add(paymentService);
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return payClass;
    }
    public List<PaymentService> getPayjsClass(String tag){
        List<PaymentService> payClass = new ArrayList<>();
        //入参 要扫描的包名
        Reflections f = new Reflections("com.food.order.plugins.pay.payjs");
        //入参 目标注解类
        Set<Class<?>> clazzes = f.getTypesAnnotatedWith(PayTarget.class);
        Iterator<Class<?>> it = clazzes.iterator();
        while (it.hasNext()) {
            Class<?> clazz = it.next();
            try {
                PluginsService pluginsService = (PluginsService)clazz.newInstance();
                if(tag == null || tag.equals(pluginsService.getPluginsTag())){
                    PaymentService paymentService = (PaymentService) clazz.newInstance();
                    payClass.add(paymentService);
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return payClass;
    }
    public List<String> getTagsByOauthTag(String tag){
        List<String> list = new ArrayList<>();
        if(tag.equals(Wechat.class.getName())){
            list.add(WxJsapi.class.getName());
            return list;
        }
        return list;
    }

    /**
     * 获取当前网络ip
     * @param request
     * @return
     */
    public String getIpAddr(HttpServletRequest request){
        String ipAddress = request.getHeader("x-forwarded-for");
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if(ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")){
                //根据网卡取本机配置的IP
                InetAddress inet=null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                ipAddress= inet.getHostAddress();
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if(ipAddress!=null && ipAddress.length()>15){ //"***.***.***.***".length() = 15
            if(ipAddress.indexOf(",")>0){
                ipAddress = ipAddress.substring(0,ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }
}

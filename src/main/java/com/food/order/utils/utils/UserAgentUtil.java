package com.food.order.utils.utils;

import com.food.order.plugins.AgnetTypeEnum;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

public class UserAgentUtil {
    /**
     * 判断 移动端/PC端
     * @Title: isMobile
     * @author: pk
     * @Description: TODO
     * @param request
     * @return
     * @return: boolean
     */
    public static boolean isMobile(HttpServletRequest request) {
        List<String> mobileAgents = Arrays.asList("ipad", "iphone os", "rv:1.2.3.4", "ucweb", "android", "windows ce", "windows mobile");
        String ua = request.getHeader("User-Agent").toLowerCase();
        for (String sua : mobileAgents) {
            if (ua.indexOf(sua) > -1) {
                return true;//手机端
            }
        }
        return false;//PC端
    }

    /**
     * 是否微信浏览器
     * @Title: isWechat
     * @author: pk
     * @Description: TODO
     * @param request
     * @return
     * @return: boolean
     */
    public static boolean isWechat(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent").toLowerCase();
        if (ua.indexOf("micromessenger") > -1) {
            return true;//微信
        }
        return false;//非微信手机浏览器

    }

    public static Integer getAgentType(HttpServletRequest request){
        if(isWechat(request)){
            return AgnetTypeEnum.WX_WEB.getIndex();
        }else{
            if(isMobile(request)){
                return AgnetTypeEnum.MOBILE_WEB.getIndex();
            }else{
                return AgnetTypeEnum.PC_WEB.getIndex();
            }
        }
    }
}

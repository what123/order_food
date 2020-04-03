package com.food.order.plugins.oauth;

import com.food.order.model.entity.OauthConfig;
import com.food.order.model.entity.OauthUser;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 第三方授权
 */
public interface OauthService {

    public String getConsumerOauthUrl();//获取客户授权路径

    public String getStoreUserOauthUrl(String state);//获取店员授权

    public OauthUser callBack(HttpServletRequest request);//回调处理

    public void setParams(OauthConfig oauthConfig,String host);



}

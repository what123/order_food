package com.food.order.plugins.oauth.wechat;

import com.food.order.model.entity.Consumer;
import com.food.order.model.entity.OauthConfig;
import com.food.order.model.entity.OauthUser;
import com.food.order.plugins.BasePlugins;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.PluginsTypeEnum;
import com.food.order.plugins.oauth.*;
import com.food.order.utils.utils.MD5Util;
import com.food.order.utils.utils.https.HttpClientUtil;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


/**
 * 微信公众号登录授权
 */
@OauthTarget
public class Wechat extends BasePlugins implements OauthService, PluginsService {
    String appid = null;
    String secret = null;
    Long uid = null;
    String host = null;
    Long oauth_id = null;
    public Wechat(){

    }



    @Override
    public String getConsumerOauthUrl() {
        String redirect_uri = host + "/api/consumer/oauth_callback/"+oauth_id;// 回调地址
        String url = null;
        try {
            url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appid+"&redirect_uri="+ URLEncoder.encode(redirect_uri,"UTF-8") +"&response_type=code&scope=snsapi_userinfo&state="+uid+"#wechat_redirect";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    public String getStoreUserOauthUrl(String state) {
        String redirect_uri = host + "/api/store_user/oauth_callback/"+oauth_id;// 回调地址
        String url = null;
        try {
            url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appid+"&redirect_uri="+ URLEncoder.encode(redirect_uri,"UTF-8") +"&response_type=code&scope=snsapi_userinfo&state="+state+"#wechat_redirect";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    public OauthUser callBack(HttpServletRequest request) {
        String code = request.getParameter("code");
        //String store_uid = request.getParameter("state");

        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appid+"&secret="+secret+"&code="+code+"&grant_type=authorization_code";
        String data = new HttpClientUtil().doGet(url, "utf-8");

        org.json.JSONObject jsonObject = new JSONObject(data);
        String openid = jsonObject.optString("openid",null);

        if(openid == null){
            return null;
        }

        String access_token = jsonObject.optString("access_token",null);
        String getUserInfo = "https://api.weixin.qq.com/sns/userinfo?access_token="+access_token+"&openid="+openid+"&lang=zh_CN";

        String userData = new HttpClientUtil().doGet(getUserInfo, "utf-8");
        //{
        //  "openid":" OPENID",
        //  " nickname": NICKNAME,
        //  "sex":"1",
        //  "province":"PROVINCE"
        //  "city":"CITY",
        //  "country":"COUNTRY",
        //  "headimgurl":       "http://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46",
        //  "privilege":[ "PRIVILEGE1" "PRIVILEGE2"     ],
        //  "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
        //}

        JSONObject jsonObject1 = new JSONObject(userData);


        OauthUser oauthUser = new OauthUser();
        //oauthUser.setOauth_user_id(jsonObject1.getString("unionid"));
        oauthUser.setUnionid(jsonObject1.optString("unionid"));
        oauthUser.setOpenid(openid);
        oauthUser.setTag(Wechat.class.getName());
        oauthUser.setNickname(jsonObject1.getString("nickname"));
        oauthUser.setPhotoUrl(jsonObject1.getString("headimgurl"));
        oauthUser.setSex(Integer.parseInt(jsonObject1.optString("sex")));
        oauthUser.setProvince(jsonObject1.getString("province"));
        oauthUser.setCity(jsonObject1.getString("city"));
        oauthUser.setCountry(jsonObject1.getString("country"));
        return oauthUser;
    }

    @Override
    public void setParams(OauthConfig oauthConfig,String host) {
        String paramsStr = oauthConfig.getParamsStr();
        appid = getParam("appid", paramsStr);
        secret = getParam("secret", paramsStr);
        this.host = host;
        if(oauthConfig.getStore() != null){
            uid = oauthConfig.getStore().getId();
        }else{
            uid = oauthConfig.getMainStore().getId();
        }
        oauth_id = oauthConfig.getId();
    }

    public String getParam(String key, String params){
        org.json.JSONObject jsonObject = new JSONObject(params);
        return jsonObject.getJSONObject(key).getString("value");
    }

    @Override
    public Map<String, String> getParamsConfig() {
        Map<String, String>  map = new HashMap<>();
        map.put("appid","平台分配的appid");
        map.put("secret","平台分配的secret");
        return map;
    }


    @Override
    public String getPluginsTag() {
        return Wechat.class.getName();
    }
    @Override
    public int getPluginsType() {
        return PluginsTypeEnum.OAUTH_PLUGINS.getIndex();
    }

    @Override
    public Integer getVip0Price() {
        return 0;
    }

    @Override
    public Integer getVip1Price() {
        return 0;
    }

    @Override
    public Integer getVip2Price() {
        return 0;
    }

    @Override
    public int getExpiryDay() {
        return 0;
    }

    @Override
    public int getFreeExpiryDay() {
        return 0;
    }

    @Override
    public String getName() {
        return "微信公众号授权";
    }

    @Override
    public String getPicPath() {
        return null;
    }

    @Override
    public String getNote() {
        return null;
    }


    @Override
    public String getPluginsUUID() {
        try {
            return MD5Util.MD5Encode(Wechat.class.getName());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}

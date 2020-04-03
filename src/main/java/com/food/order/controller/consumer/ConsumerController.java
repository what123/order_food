package com.food.order.controller.consumer;


import com.food.order.controller.BaseController;
import com.food.order.model.config.Config;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.oauth.OauthService;
import com.food.order.plugins.oauth.OauthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * Created by Administrator on 2018/4/2.
 */
@Controller("consumer_controller")
@RequestMapping("/api/consumer")
@CrossOrigin
public class ConsumerController extends BaseController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ConsumerRepository consumerRepository;
    @Autowired
    OauthConfigRepository oauthConfigRepository;
    @Autowired
    OauthUserRepository oauthUserRepository;
    @Autowired
    StoreRepository storeRepository;


    /**
     * 登录
     * @return
     */
//    @ApiOperation(value="第三方授权登录授权", notes="第三方授权登录")
//    @ApiImplicitParams({})
//    @RequestMapping(value = "/oauth_login/{oauth_id}",method = RequestMethod.GET)
//    @ResponseBody
//    public String oauth_login(@PathVariable("oauth_id")Long oauth_id){
////        Criteria<OauthConfig> criteria = new Criteria();
////        criteria.add(Restrictions.eq("id",oauth_id));
////        OauthConfig oauthConfig = oauthConfigRepository.findOne(criteria).orElse(null);
////
////        OauthService oauthService = null;
////        if (oauthConfig.getType() == OauthTypeEnum.WX_JSAPI.getIndex()){
////            oauthService = new Wechat(oauthConfig);
////        }else if (oauthConfig.getType() == OauthTypeEnum.WX_SCAN.getIndex()){
////
////        }else if (oauthConfig.getType() == OauthTypeEnum.PAYJS_JSAPI.getIndex()){
////            oauthService = new Payjs(oauthConfig);
////        }
////        return "redirect:"+oauthService.getOauthUrl();//跳转到授权
//        return null;
//    }


    /**
     * 登录
     * @return
     */
    @RequestMapping(value = "/oauth_callback/{oauth_id}",method = RequestMethod.GET)
    public String oauth_callback(HttpServletRequest request, @PathVariable("oauth_id")int oauth_id,
                                 @RequestParam("state")Long store_id,HttpSession session,
                                 HashMap<String, Object> map){
        Store store = storeRepository.findById(store_id).orElse(null);
        if(store == null){
            map.put("msg","分店不存在,授权失败");
            return "error";
        }
        Criteria<OauthConfig> criteria = new Criteria();
        criteria.add(Restrictions.eq("id",oauth_id));
        OauthConfig oauthConfig = oauthConfigRepository.findOne(criteria).orElse(null);
        OauthService oauthService = new OauthUtil().getOauthClass(oauthConfig, Config.host);
        if(oauthService == null){
            map.put("msg","授权插件不存在,授权失败");
            return "error";
        }
        PluginsService pluginsService = (PluginsService)oauthService;
        OauthUser oauthUser = oauthService.callBack(request);
        if(oauthUser != null) {
            OauthUser oauthUser1 = oauthUserRepository.findOneByOpenidAndTagAndType(oauthUser.getOpenid(), oauthUser.getTag(),OauthUserTypeEnum.CONSUMER.getIndex());
            if (oauthUser1 == null || oauthUser1.getUser().getIsFreeze() == 1 || oauthUser1.getUser().isDelete()) {//增加一个
                oauthUser.setType(OauthUserTypeEnum.CONSUMER.getIndex());
                User user = new User();
                user.setAccount(oauthUser.getOpenid()+System.currentTimeMillis());
                user.setNote(pluginsService.getName()+"授权创建用户");
                user = userRepository.saveAndFlush(user);
                oauthUser.setUser(user);
                oauthUser1 = oauthUserRepository.saveAndFlush(oauthUser);
            }
            Criteria<Consumer> consumerCriteria = new Criteria<>();
            consumerCriteria.add(Restrictions.eq("user",oauthUser1.getUser()));
            consumerCriteria.add(Restrictions.eq("store",store));
            Consumer consumer = consumerRepository.findOne(consumerCriteria).orElse(null);
            if (consumer == null){
                consumer = new Consumer();
                consumer.setStore(store);
                consumer.setUser(oauthUser1.getUser());
                consumer.setNickName(oauthUser1.getNickname());
                consumer.setMainStore(store.getMainStore());
                consumer.setWxOpenId(oauthUser1.getOpenid());
                consumer.setName(oauthUser1.getName());
                if(oauthUser1.getPhotoUrl() != null && !oauthUser1.getPhotoUrl().equals("")){
                    //TODO 头像处理
                    //oauthUser1.getUser().setPhoto();
                }
                consumer.setSex(oauthUser1.getSex());
                consumer.setProvince(oauthUser1.getProvince());
                consumer.setCity(oauthUser1.getCity());
                consumer.setCountry(oauthUser1.getCountry());

                consumerRepository.save(consumer);
            }
            removeToken(oauthUser1.getUser());
            saveToken("consumer",oauthUser1.getUser(),1);
            String token = oauthUser1.getUser().getAccessToken();
            session.setAttribute("token",token);

            String callback_url = (String)session.getAttribute("callback_url");
            if(callback_url == null || callback_url.equals("")) {
                callback_url = "/";
            }
            return "redirect:"+callback_url;
        }else {
            map.put("msg",pluginsService.getName()+"授权失败");
            return "error";
        }
    }



}

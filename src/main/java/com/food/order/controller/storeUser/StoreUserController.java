package com.food.order.controller.storeUser;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.config.Config;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.oauth.OauthService;
import com.food.order.plugins.oauth.OauthUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
@Api( value = "分店店员管理", description = "分店店员的接口",tags="店员-帐号接口")
@Controller("store_user_controller")
@RequestMapping("/api/store_user")
@CrossOrigin
public class StoreUserController extends BaseController {
    @Autowired
    StoreUserRepository storeUserRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OauthConfigRepository oauthConfigRepository;

    @Autowired
    OauthUserRepository oauthUserRepository;




    /**
     * 登录
     * @return
     */
    @ApiOperation(value="登录授权", notes="根据用户名和密码来获取店员详细信息及登录token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "账号", required = true, dataType = "String",paramType="form"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String",paramType="form"),
            @ApiImplicitParam(name = "holddays", value = "保持登录状态天数(默认2小时)", required = false, dataType = "int",paramType="form")
    })
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo login(@RequestParam("account")String account,
                       @RequestParam("password")String password,
                       @RequestParam(value = "token",defaultValue = "")String token,
                       @RequestParam(value = "holddays",defaultValue = "0")int holddays
    ){
        MsgVo msgVo = new MsgVo();
        User user = null;
        if(token.trim().equals("")) {
            Criteria<User> criteria = new Criteria<>();
            criteria.add(Restrictions.eq("account", account));
            criteria.add(Restrictions.eq("belong", 4));
            criteria.add(Restrictions.eq("isDelete", false));
            user = userRepository.findOne(criteria).orElse(null);
            if (user == null || !user.getPwd(password).equals(user.getPassword())) {
                msgVo.setCode(40002);
                msgVo.setMsg("帐号或密码错误");
                return msgVo;
            }
        }else{
            TmpCache tmpCache = tmpCacheRepository.findOneByCKey(token);
            if (tmpCache == null) {
                msgVo.setCode(40001);
                msgVo.setMsg("token不正确");
                return msgVo;
            }
            user = userRepository.findById(Long.parseLong(tmpCache.getCValue())).orElse(null);

        }

        if (user == null || user.getBelong() != 4) {
            msgVo.setCode(40001);
            msgVo.setMsg("帐号或密码错误");
            return msgVo;
        }

        Criteria<StoreUser> storeUserCriteria = new Criteria<>();
        storeUserCriteria.add(Restrictions.eq("user",user));
        StoreUser storeUser = storeUserRepository.findOne(storeUserCriteria).orElse(null);
        if(storeUser == null){
            msgVo.setCode(40003);
            msgVo.setMsg("帐号或密码错误");
            return msgVo;
        }
        if(user.getIsFreeze() == 1){
            msgVo.setCode(40004);
            msgVo.setMsg("帐号已被冻结");
            return msgVo;
        }
        if(storeUser.getStore().getUser().getIsFreeze() == 1 || storeUser.getStore().getUser().isDelete()){
            msgVo.setCode(40005);
            msgVo.setMsg("分店帐号被冻结或删除");
            return msgVo;
        }
        if(storeUser.getStore().getMainStore().getUser().getIsFreeze() == 1 || storeUser.getStore().getMainStore().getUser().isDelete()){
            msgVo.setCode(40006);
            msgVo.setMsg("总店帐号被冻结或删除");
            return msgVo;
        }
        try {
            removeToken(user);
            saveToken("store_user",user,holddays);
            msgVo.getData().put("user",storeUser);
        }catch (Exception e) {
            e.printStackTrace();
            msgVo.setCode(40007);
            msgVo.setMsg("帐号或密码错误");
            return msgVo;
        }
        return msgVo;
    }



    @ApiOperation(value="退出登录授权", notes="退出登录授权接口")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/logout",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo logout(){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        if(storeUser != null) {
            removeToken(storeUser.getUser());
        }
        return msgVo;
    }




    @ApiOperation(value="修改密码", notes="修改密码")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/setpassword",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo setpassword(@RequestParam("newpwd")String password, @RequestParam(value = "oldpwd",defaultValue = "")String oldPassword){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        User user = storeUser.getUser();

        if(!user.getPwd(oldPassword).equals(user.getPassword())){
            msgVo.setCode(40003);
            msgVo.setMsg("旧密码错误");
            return msgVo;
        }
        user.setPassword(user.getPwd(password));
        removeToken(user);
        userRepository.save(user);
        return msgVo;
    }


    /**
     * 登录
     * @return
     */
    @RequestMapping(value = "/oauth_callback/{oauth_id}",method = RequestMethod.GET)
    public String oauth_callback(HttpServletRequest request,
                                 @PathVariable("oauth_id")int oauth_id,
                                 @RequestParam(value = "state",defaultValue = "")String token,
                                 HttpSession session,
                                 HashMap<String, Object> map){
        User user = null;
        if(!token.equals("")) {//授权
            TmpCache tmpCache = tmpCacheRepository.findOneByCKey(token);
            if (tmpCache == null) {
                map.put("msg", "token不正确");
                return "error";
            }
            user = userRepository.findById(Long.parseLong(tmpCache.getCValue())).orElse(null);
            if(user == null){
                map.put("msg", "店员不存在");
                return "error";
            }
            Criteria<StoreUser> storeUserCriteria = new Criteria();
            storeUserCriteria.add(Restrictions.eq("user", user));
            StoreUser storeUser = storeUserRepository.findOne(storeUserCriteria).orElse(null);
            if (storeUser == null) {
                map.put("msg", "店员不存在,授权失败");
                return "error";
            }
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
            OauthUser oauthUser1 = oauthUserRepository.findOneByOpenidAndTagAndType(oauthUser.getOpenid(), oauthUser.getTag(),OauthUserTypeEnum.STORE_USER.getIndex());
            if (oauthUser1 == null || oauthUser1.getUser().getIsFreeze() == 1 || oauthUser1.getUser().isDelete()) {//增加一个
                if(!token.equals("")) {//授权
                    oauthUser.setType(OauthUserTypeEnum.STORE_USER.getIndex());
                    oauthUser.setUser(user);
                    oauthUserRepository.saveAndFlush(oauthUser);
                }else{//登录
                    map.put("msg","未缓定有效帐号,授权失败");
                    return "error";
                }
            }else{
                user = oauthUser1.getUser();
            }

            if(user == null){
                map.put("msg","店员不存在");
                return "error";
            }
            removeToken(user);
            saveToken("store_user",user,1);
            String token2 = user.getAccessToken();
            session.setAttribute("token",token2);

            String callback_url = (String)session.getAttribute("callback_url");
            if(callback_url == null || callback_url.equals("")) {
                callback_url = Config.host+"/admin/index.html/#/storeUser/login?token="+token2;
            }
            return "redirect:"+callback_url;
        }else {
            map.put("msg",pluginsService.getName()+"授权失败");
            return "error";
        }
    }
}

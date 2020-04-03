package com.food.order.controller.store;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.config.Config;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.plugins.oauth.OauthService;
import com.food.order.plugins.oauth.OauthUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店员管理", description = "分店店员的接口",tags="分店-店员接口")
@RestController("store_store_user_controller")
@RequestMapping("/api/store/store_user")
@CrossOrigin
public class StoreUserController extends BaseController {
    @Autowired
    StoreUserRepository storeUserRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OauthUserRepository oauthUserRepository;

    @Autowired
    OauthConfigRepository oauthConfigRepository;


    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "store_user_id",defaultValue = "0")Long store_user_id,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Criteria<StoreUser> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",store));
        if(store_user_id > 0){
            criteria.add(Restrictions.eq("id",store_user_id));
        }
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<StoreUser> storeUsers = storeUserRepository.findAll(criteria,pageable);

        List<StoreUser> storeUserList = storeUsers.getContent();
        if(storeUserList.size() > 0){
            List<User> users = new ArrayList<>();
            for (StoreUser storeUser : storeUserList) {
                users.add(storeUser.getUser());
            }
            Criteria<OauthUser> oauthUserCriteria = new Criteria<>();
            oauthUserCriteria.add(Restrictions.in("user", users));
            oauthUserCriteria.add(Restrictions.eq("type",OauthUserTypeEnum.STORE_USER.getIndex()));
            List<OauthUser> oauthUsers = oauthUserRepository.findAll(oauthUserCriteria);
            if(oauthUsers.size() > 0){
                List<Long> uids = new ArrayList<>();
                for(OauthUser oauthUser:oauthUsers){
                    uids.add(oauthUser.getUser().getId());
                }
                for (StoreUser storeUser : storeUserList) {
                    if(uids.contains(storeUser.getUser().getId())){
                        storeUser.setBindOauth(true);
                    }else{
                        storeUser.setBindOauth(false);
                    }
                }
            }

        }
        msgVo.getData().put("store_users",storeUsers);

        msgVo.getData().put("login_url", Config.host+"/admin/index.html/#/storeUser/login?t="+System.currentTimeMillis());


        //检测登录配置
        Criteria<OauthConfig> oauthConfigCriteria = new Criteria<>();
        if(store.getConfigType() == 1){//统一使用店家
            oauthConfigCriteria.add(Restrictions.eq("mainStore",store.getMainStore()));
        }else if(store.getConfigType() == 2){//使用分店
            oauthConfigCriteria.add(Restrictions.eq("store",store));
        }
        oauthConfigCriteria.add(Restrictions.eq("status",2));
        List<OauthConfig> oauthConfigs = oauthConfigRepository.findAll(oauthConfigCriteria);
        if(oauthConfigs.size() > 0) {
            OauthConfig oauthConfig = oauthConfigs.get(0);
            oauthConfig.setStore(store);
            OauthService oauthService = new OauthUtil().getOauthClass(oauthConfig, Config.host);
            if (oauthService == null) {
                msgVo.getData().put("wx_login_url", null);
            } else {
                msgVo.getData().put("wx_login_url", oauthService.getStoreUserOauthUrl(""));
            }
        }else{
            msgVo.getData().put("wx_login_url", null);
        }

        msgVo.setMsg("获取成功");
        return msgVo;
    }

    @ApiOperation(value="创建/修改", notes="创建/修改")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo add(@RequestParam(value = "id",defaultValue = "0")Long id,
                     @RequestParam("number")String number,
                     @RequestParam("account")String account,
                     @RequestParam("name")String name,
                     @RequestParam("sex")int sex,
                     @RequestParam(value = "birthday",defaultValue = "")String birthday,
                     @RequestParam(value = "telephones",defaultValue = "")String telephons,
                     @RequestParam(value = "isFreeze",defaultValue = "0")int isFreeze,
                     @RequestParam(value = "note",defaultValue = "")String note

    ){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        User user = store.getUser();
        StoreUser storeUser = null;
        if(id > 0){
            storeUser = storeUserRepository.findById(id).orElse(null);
            if(storeUser == null){
                msgVo.setCode(40002);
                msgVo.setMsg("店员不存在");
                return msgVo;
            }
        }else{
            User user1 = userRepository.findOneByAccountAndBelongAndIsDelete(account,4,false);
            if(user1 != null){
                msgVo.setCode(40002);
                msgVo.setMsg("店员帐号已存在");
                return msgVo;
            }
            storeUser = new StoreUser();
            User superUser = new User();
            superUser.setAccount(store.getMainStore().getStorePrefix()+account);
            superUser.setPassword(superUser.getPwd("888888"));
            superUser.setBelong(4);
            superUser.setNote(user.getAccount()+"分店超管创建店员帐号");
            superUser = userRepository.saveAndFlush(superUser);
            storeUser.setUser(superUser);
        }

        if(StringUtils.isNotEmpty(birthday)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                storeUser.setBirthday(simpleDateFormat.parse(birthday));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        storeUser.setNumber(number);
        storeUser.setStore(store);
        storeUser.setName(name);
        storeUser.setSex(sex);
        storeUser.setNote(note);
        storeUser.setTelephones(telephons);
        storeUser.getUser().setIsFreeze(isFreeze);
        userRepository.saveAndFlush(store.getUser());
        storeUserRepository.save(storeUser);

        return msgVo;
    }

    @ApiOperation(value="重置店员密码", notes="重置店员密码")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/reset_password/{store_user_id}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo setpassword(@PathVariable("store_user_id")Long storeUserId){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");

        StoreUser storeUser = storeUserRepository.findById(storeUserId).orElse(null);
        if(storeUser == null || storeUser.getStore().getId() != store.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("该店员不存在");
            return msgVo;
        }

        User user = storeUser.getUser();
        user.setPassword(user.getPwd("888888"));
        userRepository.save(user);
        removeToken(user);
        return msgVo;
    }

    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    @ResponseBody
    public MsgVo delete(@PathVariable("id")Long id){
        MsgVo msgVo = new MsgVo();
        //Store store = (Store) request.getAttribute("user");
        //
        StoreUser storeUser = storeUserRepository.findById(id).orElse(null);
        if(storeUser == null){
            msgVo.setCode(40001);
            msgVo.setMsg("店员不存在");
            return msgVo;
        }

        storeUser.setDelete(true);
        storeUserRepository.save(storeUser);

        User user = storeUser.getUser();
        user.setDelete(true);
        userRepository.save(user);

        Criteria<OauthUser> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("user",user));
        List<OauthUser> oauthUsers = oauthUserRepository.findAll(criteria);
        if(oauthUsers.size() > 0){
            oauthUserRepository.deleteAll(oauthUsers);
        }

        return msgVo;
    }



    @ApiOperation(value="绑定登录地址", notes="绑定登录地址")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/bind_oauth/{id}/url",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo wxOAuthUrl(@PathVariable("id")Long id){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        String msg = "";
        if(store.getConfigType() == 1){
            msg = "(当前使用总部的配置)";
        }

        //检测登录配置
        Criteria<OauthConfig> oauthConfigCriteria = new Criteria<>();
        if(store.getConfigType() == 1){//统一使用店家
            oauthConfigCriteria.add(Restrictions.eq("mainStore",store.getMainStore()));
        }else if(store.getConfigType() == 2){//使用分店
            oauthConfigCriteria.add(Restrictions.eq("store",store));
        }
        oauthConfigCriteria.add(Restrictions.eq("status",2));
        List<OauthConfig> oauthConfigs = oauthConfigRepository.findAll(oauthConfigCriteria);
        if(oauthConfigs.size() == 0){
            msgVo.setCode(40001);
            msgVo.setMsg("请先配置授权参数"+msg);
            return msgVo;
        }
        OauthConfig oauthConfig = oauthConfigs.get(0);
        oauthConfig.setStore(store);
        OauthService oauthService = new OauthUtil().getOauthClass(oauthConfig,Config.host);
        if(oauthService == null){
            msgVo.setCode(40002);
            msgVo.setMsg("登录插件不存在");
            return msgVo;
        }
        StoreUser storeUser = storeUserRepository.findById(id).orElse(null);
        if(storeUser == null || storeUser.getStore().getId() != store.getId()){
            msgVo.setCode(40003);
            msgVo.setMsg("店员不存在");
            return msgVo;
        }
        removeToken(storeUser.getUser());
        saveToken("store_user",storeUser.getUser(),0);
        msgVo.getData().put("wx_bind_url",oauthService.getStoreUserOauthUrl(storeUser.getUser().getAccessToken()));
        return msgVo;
    }

    @ApiOperation(value="解除登录地址绑定", notes="解除登录地址绑定")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/bind_oauth/{id}/url",method = RequestMethod.DELETE)
    @ResponseBody
    public MsgVo delBind(@PathVariable("id")Long id){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        StoreUser storeUser = storeUserRepository.findById(id).orElse(null);
        if(storeUser == null || storeUser.getStore().getId() != store.getId()){
            msgVo.setCode(40003);
            msgVo.setMsg("店员不存在");
            return msgVo;
        }
        //检测登录配置
        Criteria<OauthUser> oauthUserCriteria = new Criteria<>();
        oauthUserCriteria.add(Restrictions.eq("user",storeUser.getUser()));
        oauthUserCriteria.add(Restrictions.eq("type",OauthUserTypeEnum.STORE_USER.getIndex()));
        OauthUser oauthUser = oauthUserRepository.findOne(oauthUserCriteria).orElse(null);
        if(oauthUser == null){
            msgVo.setCode(40003);
            msgVo.setMsg("授权不存在");
            return msgVo;
        }
        oauthUserRepository.delete(oauthUser);
        return msgVo;
    }
}

package com.food.order.controller.mainStore;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.config.Config;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.MainStoreRepository;
import com.food.order.model.repository.OauthUserRepository;
import com.food.order.model.repository.StoreRepository;
import com.food.order.model.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
import java.util.List;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店家对分店管理", description = "店家对分店管理的接口",tags="店家-分店接口")
@RestController("main_store_store_controller")
@RequestMapping("/api/main_store/store")
@CrossOrigin
public class StoreController extends BaseController {
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OauthUserRepository oauthUserRepository;



    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({

            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "store_id",defaultValue = "0")Long store_id,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");
        Criteria<Store> criteria = new Criteria<>();
        if(store_id > 0){
            criteria.add(Restrictions.eq("id",store_id));
        }
        criteria.add(Restrictions.eq("mainStore",mainStore));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<Store> stores = storeRepository.findAll(criteria,pageable);
        msgVo.getData().put("stores",stores);

        msgVo.getData().put("login_url", Config.host+"/admin/index.html/#/store/login");

        msgVo.setMsg("获取成功");
        return msgVo;
    }

    @ApiOperation(value="创建/修改", notes="创建/修改")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo add(@RequestParam(value = "id",defaultValue = "0")Long id,
                     @RequestParam("name")String name,
                     @RequestParam("province")String province,
                     @RequestParam("city")String city,
                     @RequestParam("area")String area,
                     @RequestParam("address")String address,
                     @RequestParam(value = "name",defaultValue = "")String note,
                     @RequestParam(value = "telephons",defaultValue = "")String telephons,
                     @RequestParam(value = "startTime",defaultValue = "")String startTime,
                     @RequestParam(value = "endTime",defaultValue = "")String endTime,
                     @RequestParam(value = "configType",defaultValue = "1")int configType,//1.使用店家的支付和授权登录统一配置，2使用分店自己的支付和授权登录配置
                     @RequestParam(value = "payConfig",defaultValue = "1")int payConfig,//付费制，1预付费，2后付费制
                     @RequestParam(value = "account",defaultValue = "")String account,
                     @RequestParam(value = "isFreeze",defaultValue = "0")int isFreeze

    ){
        MsgVo msgVo = new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");
        User user = mainStore.getUser();

        Store store = null;
        if(id > 0){
            store = storeRepository.findById(id).orElse(null);
            if(store == null || store.getMainStore().getId() != mainStore.getId()){
                msgVo.setCode(40002);
                msgVo.setMsg("分店不存在");
                return msgVo;
            }
        }else{
            Criteria<Store> criteria = new Criteria<>();
            criteria.add(Restrictions.eq("mainStore",mainStore));
            List<Store> stores =  storeRepository.findAll(criteria);
            if(mainStore.getLimitStore() <= stores.size()){//限制创建
                msgVo.setCode(40002);
                msgVo.setMsg("您最多可以创建"+mainStore.getLimitStore()+"家分店，如需再创建更多分店，请联系平台管理员");
                return msgVo;
            }
            store = new Store();
            store.setMainStore(mainStore);
            User user1 = userRepository.findOneByAccountAndBelongAndIsDelete(account,3,false);
            if(user1 != null){
                msgVo.setCode(40002);
                msgVo.setMsg("分店帐号已存在");
                return msgVo;
            }
            User superUser = new User();
            superUser.setAccount(mainStore.getStorePrefix()+account);
            superUser.setPassword(superUser.getPwd("888888"));
            superUser.setBelong(3);
            superUser.setNote(user.getAccount()+"店家超管创建分店帐号");
            superUser = userRepository.saveAndFlush(superUser);
            store.setUser(superUser);
        }

        store.setName(name);
        store.setProvince(province);
        store.setCity(city);
        store.setArea(area);
        store.setAddress(address);
        store.setTelephons(telephons);
        store.setNote(note);
        store.getUser().setIsFreeze(isFreeze);
        userRepository.saveAndFlush(store.getUser());
        store.setConfigType(configType);
        store.setPayConfig(payConfig);
        store.setStartTime(startTime);
        store.setEndTime(endTime);


        storeRepository.save(store);

        return msgVo;
    }

    @ApiOperation(value="重置分店管理员密码", notes="重置分店管理员密码")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/reset_password/{store_id}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo setpassword(@PathVariable("store_id")Long storeId){
        MsgVo msgVo = new MsgVo();
        Store store = storeRepository.findById(storeId).orElse(null);
        MainStore mainStore = (MainStore) request.getAttribute("user");
        if(store == null || store.getMainStore().getId() != mainStore.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("该分店管理员不存在");
            return msgVo;
        }

        User user = store.getUser();
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
        MainStore mainStore = (MainStore) request.getAttribute("user");
        Store store = storeRepository.findById(id).orElse(null);
        if(store == null || store.getMainStore().getId() != mainStore.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("店家不存在");
            return msgVo;
        }
        store.setDelete(true);
        storeRepository.save(store);

        User user = store.getUser();
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

}

package com.food.order.controller.superAdmin;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.MainStore;
import com.food.order.model.entity.SuperAdmin;
import com.food.order.model.entity.User;
import com.food.order.model.repository.MainStoreRepository;
import com.food.order.model.repository.SuperAdminRepository;
import com.food.order.model.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import sun.applet.Main;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店家管理", description = "后台店家的接口",tags="平台超管-店家管理接口")
@RestController("super_admin_main_store_controller")
@RequestMapping("/api/super_admin/main_store")
@CrossOrigin
public class MainStoreController extends BaseController {
    @Autowired
    MainStoreRepository mainStoreRepository;
    @Autowired
    UserRepository userRepository;



    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(

            @RequestParam(value = "id",defaultValue = "0")Long id,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        SuperAdmin superAdmin = (SuperAdmin) request.getAttribute("user");
        Criteria<MainStore> criteria = new Criteria<>();
        if(id > 0){
            criteria.add(Restrictions.eq("id",id));
        }
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<MainStore> mainStores = mainStoreRepository.findAll(criteria,pageable);
        msgVo.getData().put("main_store",mainStores);
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
                     @RequestParam("telephons")String telephons,
                     @RequestParam("limitStore")int limitStore,
                     @RequestParam(value = "storePrefix",defaultValue = "")String storePrefix,
                     @RequestParam(value = "account",defaultValue = "")String account,
                     @RequestParam(value = "isFreeze",defaultValue = "0")int isFreeze,
                     @RequestParam(value = "note",defaultValue = "")String note

    ){
        MsgVo msgVo = new MsgVo();

        MainStore mainStore = null;
        if(id > 0){
            mainStore = mainStoreRepository.findById(id).orElse(null);
            if(mainStore == null){
                msgVo.setCode(40002);
                msgVo.setMsg("店家不存在");
                return msgVo;
            }
        }else{
            mainStore = new MainStore();
            User superUser = new User();
            superUser.setAccount(account);
            superUser.setPassword(superUser.getPwd("888888"));
            superUser.setBelong(2);
            superUser.setNote("平台超管创建店家帐号");
            superUser = userRepository.saveAndFlush(superUser);
            mainStore.setUser(superUser);
        }
        mainStore.setStorePrefix(storePrefix);
        mainStore.setName(name);
        mainStore.setProvince(province);
        mainStore.setCity(city);
        mainStore.setArea(area);
        mainStore.setAddress(address);
        mainStore.setTelephons(telephons);
        mainStore.setLimitStore(limitStore);
        mainStore.getUser().setIsFreeze(isFreeze);
        userRepository.saveAndFlush(mainStore.getUser());
        mainStore.setNote(note);

        mainStoreRepository.save(mainStore);

        return msgVo;
    }

    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    @ResponseBody
    public MsgVo delete(@PathVariable("id")Long id){
        MsgVo msgVo = new MsgVo();
        SuperAdmin superAdmin = (SuperAdmin) request.getAttribute("user");
        MainStore mainStore = mainStoreRepository.findById(id).orElse(null);
        if(mainStore == null){
            msgVo.setCode(40001);
            msgVo.setMsg("店家不存在");
            return msgVo;
        }
        mainStoreRepository.delete(mainStore);
        return msgVo;
    }

}

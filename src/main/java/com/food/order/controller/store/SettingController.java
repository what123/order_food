package com.food.order.controller.store;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.StoreRepository;
import com.food.order.model.repository.UserRepository;
import com.food.order.model.repository.UserSettingRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "分店管理员系统设置管理", description = "分店管理员的系统设置接口",tags="分店-系统设置接口")
@RestController("store_setting_controller")
@RequestMapping("/api/store/setting")
@CrossOrigin
public class SettingController extends BaseController {
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserSettingRepository userSettingRepository;




    @ApiOperation(value="获取设置数据", notes="获取设置数据")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo getSetting(){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");

        Criteria<UserConfigs> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",store));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        List<UserConfigs> settings = userSettingRepository.findAll(criteria,sort);
        Map<Integer, UserConfigs> settingMap = new HashMap<>();
        if(settings.size() > 0){
            for(UserConfigs setting:settings){
                settingMap.put(setting.getValueKey(),setting);
            }
        }

        for(UserConfigTypeEnum settingTypeEnum: UserConfigTypeEnum.values()){
            if(!settingMap.containsKey(settingTypeEnum.getIndex())){
                UserConfigs setting = new UserConfigs();
                setting.setName(settingTypeEnum.getName());
                setting.setValueStr(settingTypeEnum.getSettingParams().get(0).getKey());
                setting.setStore(store);
                setting.setSettingParams(settingTypeEnum.getSettingParams());
                setting.setNote(settingTypeEnum.getNote());
                setting.setType(settingTypeEnum.getType());
                setting.setValueKey(settingTypeEnum.getIndex());
                setting = userSettingRepository.saveAndFlush(setting);
                settings.add(setting);
            }else{
                UserConfigs setting = settingMap.get(settingTypeEnum.getIndex());
                setting.setName(settingTypeEnum.getName());
                setting.setType(settingTypeEnum.getType());
                setting.setSettingParams(settingTypeEnum.getSettingParams());
                setting.setNote(settingTypeEnum.getNote());
            }
        }

        msgVo.getData().put("settings",settings);
        return msgVo;
    }



    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{key}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo setting(@PathVariable("key")int key,@RequestParam("value")String value){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Criteria<UserConfigs> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",store));
        criteria.add(Restrictions.eq("valueKey",key));
        UserConfigs setting = userSettingRepository.findOne(criteria).orElse(null);
        setting.setValueStr(value);
        userSettingRepository.save(setting);
        return msgVo;
    }


}

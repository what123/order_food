package com.food.order.controller.storeUser;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.entity.Store;
import com.food.order.model.entity.StoreUser;
import com.food.order.model.repository.StoreRepository;
import com.food.order.model.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "分店管理员系统设置管理", description = "分店管理员的系统设置接口",tags="分店-系统设置接口")
@RestController("store_user_setting_controller")
@RequestMapping("/api/store_user/setting")
@CrossOrigin
public class SettingController extends BaseController {
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    UserRepository userRepository;





    @ApiOperation(value="获取设置数据", notes="获取设置数据")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo getSetting(){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        msgVo.getData().put("setting",storeUser.getStore());
        return msgVo;
    }



}

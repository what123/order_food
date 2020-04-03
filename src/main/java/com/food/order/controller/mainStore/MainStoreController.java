package com.food.order.controller.mainStore;


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
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店家管理员管理", description = "店家管理员的接口",tags="店家-帐号接口")
@RestController("main_store_controller")
@RequestMapping("/api/main_store")
@CrossOrigin
public class MainStoreController extends BaseController {
    @Autowired
    MainStoreRepository mainStoreRepository;
    @Autowired
    UserRepository userRepository;




    /**
     * 登录
     * @return
     */
    @ApiOperation(value="店家管理员登录授权", notes="根据用户名和密码来获取店家管理员详细信息及登录token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "账号", required = true, dataType = "String",paramType="form"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String",paramType="form"),
            @ApiImplicitParam(name = "holddays", value = "保持登录状态天数(默认2小时)", required = false, dataType = "int",paramType="form")
    })
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo login(@RequestParam("account")String account,
                       @RequestParam("password")String password,
                       @RequestParam(value = "holddays",defaultValue = "0")int holddays
    ){
        MsgVo msgVo = new MsgVo();
        Criteria<User> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("account",account));
        //criteria.add(Restrictions.eq("password",password));
        criteria.add(Restrictions.eq("belong",2));
        criteria.add(Restrictions.eq("isDelete",false));
        User user = userRepository.findOne(criteria).orElse(null);
        if(user == null){
            msgVo.setCode(40001);
            msgVo.setMsg("帐号或密码错误");
            return msgVo;
        }
        if(!user.getPwd(password).equals(user.getPassword())){
            msgVo.setCode(40002);
            msgVo.setMsg("帐号或密码错误");
            return msgVo;
        }
        Criteria<MainStore> superAdminCriteria = new Criteria<>();
        superAdminCriteria.add(Restrictions.eq("user",user));
        MainStore mainStore = mainStoreRepository.findOne(superAdminCriteria).orElse(null);
        if(mainStore == null){
            msgVo.setCode(40003);
            msgVo.setMsg("帐号或密码错误");
            return msgVo;
        }
        if(user.getIsFreeze() == 1){
            msgVo.setCode(40005);
            msgVo.setMsg("帐号已被冻结");
            return msgVo;
        }
        try {
            removeToken(user);
            saveToken("main_store",user,holddays);
            msgVo.getData().put("user",mainStore);
        }catch (Exception e) {
            e.printStackTrace();
            msgVo.setCode(40004);
            msgVo.setMsg("帐号或密码错误");
            return msgVo;
        }
        return msgVo;
    }



    @ApiOperation(value="店家管理员退出登录授权", notes="店家管理员退出登录授权接口")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/logout",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo logout(){
        MsgVo msgVo = new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");
        if(mainStore != null) {
            removeToken(mainStore.getUser());
        }
        return msgVo;
    }




    @ApiOperation(value="修改店家管理员密码", notes="修改店家管理员密码")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/setpassword",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo setpassword(@RequestParam("newpwd")String password, @RequestParam(value = "oldpwd",defaultValue = "")String oldPassword){
        MsgVo msgVo = new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");
        User user = mainStore.getUser();

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


}

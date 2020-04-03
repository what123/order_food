package com.food.order.controller.store;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.MainStore;
import com.food.order.model.entity.Store;
import com.food.order.model.entity.User;
import com.food.order.model.repository.MainStoreRepository;
import com.food.order.model.repository.StoreRepository;
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
@Api( value = "分店管理员管理", description = "分店管理员的接口",tags="分店-帐号接口")
@RestController("store_controller")
@RequestMapping("/api/store")
@CrossOrigin
public class StoreController extends BaseController {
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    UserRepository userRepository;




    /**
     * 登录
     * @return
     */
    @ApiOperation(value="登录授权", notes="根据用户名和密码来获取店家管理员详细信息及登录token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "账号", required = true, dataType = "String",paramType="form"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String",paramType="form"),
            @ApiImplicitParam(name = "holddays", value = "保持登录状态天数(默认2小时)", required = false, dataType = "int",paramType="form")
    })
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo login(@RequestParam("account")String account,
                       @RequestParam("password")String password,
                       @RequestParam(value = "holddays",defaultValue = "0")int holddays){
        MsgVo msgVo = new MsgVo();
        Criteria<User> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("account",account));
        criteria.add(Restrictions.eq("belong",3));
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
        Criteria<Store> storeCriteria = new Criteria<>();
        storeCriteria.add(Restrictions.eq("user",user));
        Store store = storeRepository.findOne(storeCriteria).orElse(null);
        if(store == null){
            msgVo.setCode(40003);
            msgVo.setMsg("帐号或密码错误");
            return msgVo;
        }
        if(user.getIsFreeze() == 1){
            msgVo.setCode(40005);
            msgVo.setMsg("帐号已被冻结");
            return msgVo;
        }
        if(store.getMainStore().getUser().getIsFreeze() == 1 || store.getMainStore().getUser().isDelete()){
            msgVo.setCode(40006);
            msgVo.setMsg("总店帐号被冻结或删除");
            return msgVo;
        }
        try {
            removeToken(user);
            saveToken("store",user,holddays);
            msgVo.getData().put("user",store);
        }catch (Exception e) {
            e.printStackTrace();
            msgVo.setCode(40004);
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
        Store store = (Store) request.getAttribute("user");
        if(store != null) {
            removeToken(store.getUser());
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
        Store store = (Store) request.getAttribute("user");
        User user = store.getUser();
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

package com.food.order.controller.superAdmin;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.MainStore;
import com.food.order.model.entity.SuperAdmin;
import com.food.order.model.entity.TmpCache;
import com.food.order.model.entity.User;
import com.food.order.model.repository.MainStoreRepository;
import com.food.order.model.repository.SuperAdminRepository;
import com.food.order.model.repository.TmpCacheRepository;
import com.food.order.model.repository.UserRepository;
import com.food.order.utils.utils.MD5Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "管理员管理", description = "后台管理员的接口",tags="平台超管-帐号接口")
@RestController("super_admin_controller")
@RequestMapping("/api/super_admin")
@CrossOrigin
public class SuperAdminController extends BaseController {
    @Autowired
    SuperAdminRepository superAdminRepository;
    @Autowired
    UserRepository userRepository;




    /**
     * 登录
     * @return
     */
    @ApiOperation(value="后台管理员登录授权", notes="根据用户名和密码来获取管理员详细信息及登录token")
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
        criteria.add(Restrictions.eq("belong",1));

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

        Criteria<SuperAdmin> superAdminCriteria = new Criteria<>();
        superAdminCriteria.add(Restrictions.eq("user",user));
        SuperAdmin superAdmin = superAdminRepository.findOne(superAdminCriteria).orElse(null);
        if(superAdmin == null){
            msgVo.setCode(40003);
            msgVo.setMsg("帐号或密码错误");
            return msgVo;
        }
        try {
            removeToken(user);
            saveToken("super_admin",user,holddays);
            msgVo.getData().put("user",superAdmin);
        }catch (Exception e) {
            e.printStackTrace();
            msgVo.setCode(40004);
            msgVo.setMsg("帐号或密码错误");
            return msgVo;
        }
        return msgVo;
    }



    @ApiOperation(value="后台管理员退出登录授权", notes="后台管理员退出登录授权接口")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/logout",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo logout(){
        MsgVo msgVo = new MsgVo();
        SuperAdmin superAdmin = (SuperAdmin) request.getAttribute("user");
        if(superAdmin != null) {
            removeToken(superAdmin.getUser());
        }
        return msgVo;
    }




    @ApiOperation(value="修改管理员密码", notes="修改管理员密码")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/setpassword",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo setpassword(@RequestParam("newpwd")String password, @RequestParam(value = "oldpwd")String oldPassword){
        MsgVo msgVo = new MsgVo();
        SuperAdmin superAdmin = (SuperAdmin) request.getAttribute("user");
        User user = superAdmin.getUser();

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

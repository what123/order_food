package com.food.order.controller.store;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
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
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "分店黑名单", description = "分店黑名单",tags="分店-黑名单接口")
@RestController("store_blacklist_controller")
@RequestMapping("/api/store/blacklist")
@CrossOrigin
public class BlacklistController extends BaseController {
    @Autowired
    ConsumerRepository consumerRepository;
    @Autowired
    BlacklistRepository blacklistRepository;
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
            @RequestParam(value = "user_id",defaultValue = "0")Long user_id,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Criteria<Blacklist> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",store));
        if(user_id > 0){
            User user = userRepository.findById(user_id).orElse(null);
            Criteria<Consumer> criteria1 = new Criteria<>();
            criteria1.add(Restrictions.eq("user",user));
            Consumer consumer = consumerRepository.findOne(criteria1).orElse(null);
            if(consumer == null){
                msgVo.setCode(40001);
                msgVo.setMsg("客户不存在");
                return msgVo;
            }
            criteria.add(Restrictions.eq("consumer",consumer));
        }
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<Blacklist> blacklists = blacklistRepository.findAll(criteria,pageable);
        msgVo.getData().put("blacklists",blacklists);
        msgVo.setMsg("获取成功");
        return msgVo;
    }



    @ApiOperation(value="创建", notes="创建")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo add(@RequestParam(value = "user_id")Long user_id){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        User user = userRepository.findById(user_id).orElse(null);
        Criteria<Consumer> criteria1 = new Criteria<>();
        criteria1.add(Restrictions.eq("user",user));
        Consumer consumer = consumerRepository.findOne(criteria1).orElse(null);
        if(consumer == null){
            msgVo.setCode(40001);
            msgVo.setMsg("客户不存在");
            return msgVo;
        }
        Criteria<Blacklist> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",store));
        criteria.add(Restrictions.eq("consumer",consumer));
        Blacklist blacklist = blacklistRepository.findOne(criteria).orElse(null);
        if(blacklist == null) {
            blacklist = new Blacklist();
            blacklist.setConsumer(consumer);
            blacklist.setStore(store);
            blacklistRepository.save(blacklist);
        }else{
            msgVo.setCode(40002);
            msgVo.setMsg("该帐号已经在黑名单中了");
            return msgVo;
        }
        return msgVo;
    }


    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "",method = RequestMethod.DELETE)
    @ResponseBody
    public MsgVo delete(@RequestParam(value = "user_id")Long user_id){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");

        User user = userRepository.findById(user_id).orElse(null);
        Criteria<Consumer> criteria1 = new Criteria<>();
        criteria1.add(Restrictions.eq("user",user));
        Consumer consumer = consumerRepository.findOne(criteria1).orElse(null);
        if(consumer == null){
            msgVo.setCode(40001);
            msgVo.setMsg("客户不存在");
            return msgVo;
        }
        Criteria<Blacklist> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",store));
        criteria.add(Restrictions.eq("consumer",consumer));
        Blacklist blacklist = blacklistRepository.findOne(criteria).orElse(null);

        if(blacklist == null || blacklist.getStore().getId() != store.getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("该用户不存黑名单中");
            return msgVo;
        }
        blacklistRepository.delete(blacklist);
        return msgVo;
    }

}

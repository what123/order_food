package com.food.order.controller.store;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.BlacklistRepository;
import com.food.order.model.repository.ConsumerRepository;
import com.food.order.model.repository.UserRepository;
import com.food.order.model.repository.VipUserCardRepository;
import com.food.order.model.service.PluginsServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "会员管理", description = "会员的接口",tags="分店-会员管理接口")
@RestController("store_consumer_controller")
@RequestMapping("/api/store/consumers")
@CrossOrigin
public class ConsumerController extends BaseController {

    @Autowired
    ConsumerRepository consumerRepository;

    @Autowired
    PluginsServiceImpl pluginsServiceImpl;
    @Autowired
    BlacklistRepository blacklistRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    VipUserCardRepository vipUserCardRepository;


    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list( @RequestParam(value = "page",defaultValue = "1")Integer page,
                       @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                       @RequestParam(value = "keyword",defaultValue = "")String keyword,
                       HttpServletRequest request){
        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");

        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Criteria<Consumer> consumerCriteria = new Criteria<>();
        consumerCriteria.add(Restrictions.eq("store",store));


        if(!keyword.trim().equals("")){
            Criteria<User> userCriteria = new Criteria<>();
            userCriteria.add(Restrictions.like("account","%"+keyword+"%"));
            userCriteria.add(Restrictions.eq("isFreeze",0));
            List<User> users = userRepository.findAll(userCriteria);
            if(users .size() > 0) {
                consumerCriteria.add(Restrictions.or(Restrictions.like("name", "%" + keyword + "%"), Restrictions.like("nickName", "%" + keyword + "%"), Restrictions.in("user", users)));
            }else{
                consumerCriteria.add(Restrictions.or(Restrictions.like("name", "%" + keyword + "%"), Restrictions.like("nickName", "%" + keyword + "%")));
            }

        }
        Page<Consumer> consumers = consumerRepository.findAll(consumerCriteria,pageable);

        List<Consumer> consumersList = consumers.getContent();
        if(consumersList.size() > 0) {
            Criteria<Blacklist> blacklistCriteria = new Criteria<>();
            blacklistCriteria.add(Restrictions.eq("store", store));
            blacklistCriteria.add(Restrictions.in("consumer", consumers.getContent()));
            List<Blacklist> blacklist = blacklistRepository.findAll(blacklistCriteria);
            if (blacklist != null && blacklist.size() >0) {
                Map<Long,Blacklist> blacklistMap = new HashMap<>();
                for(Blacklist blacklist1:blacklist){
                    blacklistMap.put(blacklist1.getConsumer().getId(),blacklist1);
                }
                for(Consumer consumer:consumersList){
                    if(blacklistMap.containsKey(consumer.getId())){
                        consumer.setBlack(true);
                    }
                }

            }
        }
        msgVo.getData().put("consumers",consumers);
        msgVo.setMsg("获取成功");
        return msgVo;
    }

    @ApiOperation(value="充值", notes="充值")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo add(@PathVariable(value = "id")Long id,
                     @RequestParam("chargePrice")Integer chargePrice){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Consumer consumer = consumerRepository.findById(id).orElse(null);
        if(consumer == null || consumer.getStore().getId() != store.getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("用户不存在");
            return msgVo;
        }

        Criteria<VipUserCard> vipUserCardCriteria = new Criteria<>();
        vipUserCardCriteria.add(Restrictions.eq("consumer",consumer));

        VipUserCard vipUserCard = vipUserCardRepository.findOne(vipUserCardCriteria).orElse(null);

        if(vipUserCard != null){
            if(vipUserCard.getBalance() == null) {
                vipUserCard.setBalance(0);
            }
            vipUserCard.setBalance(vipUserCard.getBalance() + chargePrice);
            vipUserCardRepository.saveAndFlush(vipUserCard);
        }else{
            msgVo.setCode(40003);
            msgVo.setMsg("用户未领取会员卡，请先领取");
        }

        return msgVo;
    }


}

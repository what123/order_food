package com.food.order.controller.consumer;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.plugins.AgnetTypeEnum;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.pay.PayOrCollectEnum;
import com.food.order.plugins.pay.PayUtil;
import com.food.order.plugins.pay.PaymentService;
import com.food.order.plugins.printer.PrinterDataModeTypeEnum;
import com.food.order.utils.utils.UserAgentUtil;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "消费者获取支付展示信息", description = "消费者获取支付展示信息",tags = "消费者-支付相关接口")
@RestController("consumer_payment_config_controller")
@RequestMapping("/api/consumer/payment_config")
@CrossOrigin
public class PaymentsConfigController extends BaseController {
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    GoodsTagRepository goodsTagRepository;
    @Autowired
    PaymentsConfigRepository paymentsConfigRepository;
    @Autowired
    OauthUserRepository oauthUserRepository;



    @ApiOperation(value="可使用的支付列表", notes="可使用的支付列表")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "/can_used",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Consumer consumer = (Consumer) request.getAttribute("user");


        List<String> paymentTags = new ArrayList<>();
        paymentTags.add("-1");
        int type = UserAgentUtil.getAgentType(request); // 当前浏览器
        PayUtil payUtil = new PayUtil();
        List<PaymentService> paymentServices = payUtil.getPaymentClass(null);

        // 通过当前登录的帐号判断用哪种支付
//        Criteria<OauthUser> oauthUserCriteria = new Criteria<>();
//        oauthUserCriteria.add(Restrictions.eq("user",consumer.getUser()));
//        OauthUser oauthUser = oauthUserRepository.findOne(oauthUserCriteria).orElse(null);//
//        if(oauthUser == null){//只能H5支付
            for(PaymentService paymentService:paymentServices){
                if(paymentService.canPayAgentTypes().contains(type) && paymentService.getPayOrCollect() == PayOrCollectEnum.Pay.getIndex()){
                    PluginsService pluginsService = (PluginsService)paymentService;
                    paymentTags.add(pluginsService.getPluginsTag());
                }
            }
//        }else{
//            List<Integer> oauthPaymentTypes = payUtil.getTypeByOauthType(oauthUser.getType());//通过授权方式获取对应的支付方式
//            for(PaymentService paymentService:paymentServices){
//                if(paymentService.canPayAgentTypes().contains(type) && oauthPaymentTypes.contains(paymentService.getPaymentType()) && paymentService.getPayOrCollect() == PayOrCollectEnum.Pay.getIndex()){//浏览器和支付授权都相对应
//                    paymentTypes.add(paymentService.getPaymentType());
//                }
//            }
//        }
        if(consumer.getStore().getPayConfig() == 1){//预计付费，则要返回支付跳转地址
            msgVo.getData().put("pay_now",true);//是否要马上支付
        }else{//后付费机制
            msgVo.getData().put("pay_now",false);
        }




        Criteria<PaymentsConfig> criteria = new Criteria<>();
        criteria.add(Restrictions.in("tag",paymentTags));
        if(consumer.getStore().getConfigType() == 1){//使用总部的统一配置
            criteria.add(Restrictions.eq("mainStore",consumer.getStore().getMainStore()));
        }else{
            criteria.add(Restrictions.eq("store",consumer.getStore()));
        }
        criteria.add(Restrictions.eq("status",2));
        List<PaymentsConfig> paymentsConfigs = paymentsConfigRepository.findAll(criteria);


        msgVo.getData().put("paymentsConfigs",paymentsConfigs);
        msgVo.setMsg("获取成功");
        return msgVo;
    }
}

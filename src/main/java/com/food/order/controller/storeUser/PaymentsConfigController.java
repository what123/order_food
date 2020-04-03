package com.food.order.controller.storeUser;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.Consumer;
import com.food.order.model.entity.OauthUser;
import com.food.order.model.entity.PaymentsConfig;
import com.food.order.model.entity.StoreUser;
import com.food.order.model.repository.*;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.pay.PayOrCollectEnum;
import com.food.order.plugins.pay.PayUtil;
import com.food.order.plugins.pay.PaymentService;
import com.food.order.utils.utils.UserAgentUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "获取支付展示信息", description = "获取支付展示信息",tags = "店员-支付相关接口")
@RestController("store_user_payment_config_controller")
@RequestMapping("/api/store_user/payment_config")
@CrossOrigin
public class PaymentsConfigController extends BaseController {
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
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        List<String> paymentTypes = new ArrayList<>();
        paymentTypes.add("-1");
        PayUtil payUtil = new PayUtil();
        List<PaymentService> paymentServices = payUtil.getPaymentClass(null);
        for(PaymentService paymentService:paymentServices){
            if(paymentService.getPayOrCollect() == PayOrCollectEnum.Collect.getIndex()){
                PluginsService pluginsService = (PluginsService)paymentService;
                paymentTypes.add(pluginsService.getPluginsTag());
            }
        }
        Criteria<PaymentsConfig> criteria = new Criteria<>();

        criteria.add(Restrictions.eq("status", 2));
        criteria.add(Restrictions.in("tag",paymentTypes));
        if(storeUser.getStore().getConfigType() == 1){//使用总部的统一配置
            criteria.add(Restrictions.eq("mainStore",storeUser.getStore().getMainStore()));
        }else{
            criteria.add(Restrictions.eq("store",storeUser.getStore()));
        }

        List<PaymentsConfig> paymentsConfigs = paymentsConfigRepository.findAll(criteria);
        msgVo.getData().put("paymentsConfigs",paymentsConfigs);
        msgVo.setMsg("获取成功");
        return msgVo;
    }
}

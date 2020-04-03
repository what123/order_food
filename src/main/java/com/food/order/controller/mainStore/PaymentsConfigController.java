package com.food.order.controller.mainStore;


import com.alibaba.fastjson.JSON;
import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.config.Config;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.model.service.PluginsServiceImpl;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.PluginsTypeEnum;
import com.food.order.plugins.pay.PayUtil;
import com.food.order.plugins.pay.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店家支付配置管理", description = "店家支付配置的接口",tags="店家-支付管理接口")
@RestController("main_store_payment_config_controller")
@RequestMapping("/api/main_store/payment_config")
@CrossOrigin
public class PaymentsConfigController extends BaseController {
    @Autowired
    PaymentsConfigRepository paymentsConfigRepository;
    @Autowired
    PluginsServiceImpl pluginsServiceImpl;


    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "id",defaultValue = "0")Long id,
            HttpServletRequest request) throws ClassNotFoundException {

        MsgVo msgVo =new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");

        List<Plugins> plugins = pluginsServiceImpl.getMainStoreAllPlugins(mainStore,PluginsTypeEnum.PAY_PLUGINS.getIndex());
        Map<String,Plugins> pluginsMap = new HashMap<>();
        for (Plugins plugins1:plugins) {
            pluginsMap.put(plugins1.getUuid(),plugins1);
        }

        Criteria<PaymentsConfig> criteria = new Criteria<>();
        if(id > 0){
            criteria.add(Restrictions.eq("id",id));
        }
        criteria.add(Restrictions.eq("mainStore",mainStore));
        Sort sort = new Sort(Sort.Direction.DESC, "name");
        List<PaymentsConfig> paymentsConfigs = paymentsConfigRepository.findAll(criteria,sort);

        List<PaymentService> payClass = new PayUtil().getPaymentClass(null);
        //初始化数据
        Map<String,PaymentsConfig> tags = new HashMap<>();
        if (paymentsConfigs != null && paymentsConfigs.size() > 0) {
            for (PaymentsConfig paymentsConfig : paymentsConfigs) {
                tags.put(paymentsConfig.getTag(),paymentsConfig);
            }

        }
        for (PaymentService paymentService : payClass) {
            PluginsService pluginsService = (PluginsService)paymentService;
            if (!tags.containsKey(pluginsService.getPluginsTag())) {
                PaymentsConfig paymentsConfig = new PaymentsConfig();
                paymentsConfig.setMainStore(mainStore);
                paymentsConfig.setTag(pluginsService.getPluginsTag());
                paymentsConfig.setName(pluginsService.getName());
                Map<String, String> map = ((PluginsService) paymentService).getParamsConfig();
                Map<String, Map<String, Object>> map2 = new HashMap<>();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    Map<String, Object> values = new HashMap<>();
                    String key = entry.getKey();
                    if(entry.getKey().startsWith("UPLOADFILE_")){//上传文件
                        values.put("ele_type","file");
                        key = key.substring(11);
                    }else{//普通类型
                        values.put("ele_type","text");
                    }

                    values.put("value", "");
                    values.put("placeholder", entry.getValue());
                    map2.put(key, values);
                }
                paymentsConfig.setParams(JSON.toJSONString(map2));
                Plugins plugins1 = pluginsServiceImpl.getNewPlugins(mainStore,null,pluginsService);
                paymentsConfig.setPlugins(plugins1);
                paymentsConfig = paymentsConfigRepository.saveAndFlush(paymentsConfig);
                paymentsConfigs.add(paymentsConfig);
            }else{
                if(pluginsMap.containsKey(pluginsService.getPluginsUUID())){
                    Plugins plugins1 = pluginsMap.get(pluginsService.getPluginsUUID());
                    pluginsServiceImpl.setPluginPrices(mainStore,null,plugins1,pluginsService);

                    tags.get(pluginsService.getPluginsTag()).setPlugins(plugins1);
                }else{
                    Plugins plugins1 = pluginsServiceImpl.getNewPlugins(mainStore,null,pluginsService);
                    tags.get(pluginsService.getPluginsTag()).setPlugins(plugins1);
                }

            }
        }

        msgVo.getData().put("paymentsConfigs",paymentsConfigs);
        msgVo.getData().put("jsapi_url", Config.host+"/api/payment/pay/");
        msgVo.setMsg("获取成功");
        return msgVo;
    }


    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo add(@PathVariable(value = "id")Long id,
                     @RequestParam("status")int status,
                     @RequestParam("params")String params
    ){
        MsgVo msgVo = new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");

        PaymentsConfig paymentsConfig = null;
        if(id > 0){
            paymentsConfig = paymentsConfigRepository.findById(id).orElse(null);
            if(paymentsConfig == null || paymentsConfig.getMainStore().getId() != mainStore.getId()){
                msgVo.setCode(40002);
                msgVo.setMsg("该配置不存在");
                return msgVo;
            }
            //检测对应的插件是否购买或过期
            List<PaymentService> paymentServices = new PayUtil().getPaymentClass(paymentsConfig.getTag());
            if(paymentServices.size() == 0){
                msgVo.setCode(40004);
                msgVo.setMsg("必须传正确的id");
                return msgVo;
            }
            PaymentService paymentService = paymentServices.get(0);
            MsgVo msgVo1 = pluginsServiceImpl.checkPlugins(mainStore,null,paymentService.getClass().getName());
            if(msgVo1.getCode() != 0){//过期或未购买
                return msgVo1;
            }

        }else{
            msgVo.setCode(40003);
            msgVo.setMsg("必须传正确的id");
            return msgVo;
        }

        paymentsConfig.setStatus(status);
        paymentsConfig.setParams(params);
        paymentsConfigRepository.save(paymentsConfig);
        return msgVo;
    }
}

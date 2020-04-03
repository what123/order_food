package com.food.order.controller.store;


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
import com.food.order.plugins.pay.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "分店支付配置管理", description = "分店支付配置的接口",tags="分店-支付配置接口")
@RestController("store_payment_config_controller")
@RequestMapping("/api/store/payment_config")
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
    PluginsServiceImpl pluginsServiceImpl;

    @Autowired
    PluginsRepository pluginsRepository;



    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "id",defaultValue = "0")Long id,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");
        if(store.getConfigType() == 1) {
            msgVo.setCode(40002);
            msgVo.setMsg("统一使用总部的配置");
            return msgVo;
        }

        List<Plugins> plugins = pluginsServiceImpl.getStoreAllPlugins(store,PluginsTypeEnum.PAY_PLUGINS.getIndex());
        Map<String,Plugins> pluginsMap = new HashMap<>();
        for (Plugins plugins1:plugins) {
            pluginsMap.put(plugins1.getUuid(),plugins1);
        }

        Criteria<PaymentsConfig> criteria = new Criteria<>();
        if(id > 0){
            criteria.add(Restrictions.eq("id",id));
        }
        criteria.add(Restrictions.eq("store", store));
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
            PluginsService pluginsService = (PluginsService) paymentService;
            if (!tags.containsKey(pluginsService.getPluginsTag())) {
                PaymentsConfig paymentsConfig = new PaymentsConfig();
                paymentsConfig.setStore(store);
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

                if(pluginsMap.containsKey(pluginsService.getPluginsUUID())){
                    Plugins plugins1 = pluginsMap.get(pluginsService.getPluginsUUID());
                    pluginsServiceImpl.setPluginPrices(null,store,plugins1,pluginsService);
                    paymentsConfig.setPlugins(plugins1);
                }else {
                    Plugins plugins1 = pluginsServiceImpl.getNewPlugins(null,store,pluginsService);
                    paymentsConfig.setPlugins(plugins1);
                }
                paymentsConfig = paymentsConfigRepository.saveAndFlush(paymentsConfig);
                paymentsConfigs.add(paymentsConfig);
            }else{
                if(pluginsMap.containsKey(pluginsService.getPluginsUUID())){
                    Plugins plugins1 = pluginsMap.get(pluginsService.getPluginsUUID());
                    pluginsServiceImpl.setPluginPrices(null,store,plugins1,pluginsService);
                    tags.get(pluginsService.getPluginsTag()).setPlugins(plugins1);
                }else{
                    Plugins plugins1 = pluginsServiceImpl.getNewPlugins(null,store,pluginsService);
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
        Store store = (Store) request.getAttribute("user");
        if(store.getConfigType() == 1) {
            msgVo.setCode(40002);
            msgVo.setMsg("统一使用总部的配置");
            return msgVo;
        }

        PaymentsConfig paymentsConfig = null;
        if(id > 0){
            paymentsConfig = paymentsConfigRepository.findById(id).orElse(null);
            if(paymentsConfig == null || paymentsConfig.getStore() == null || paymentsConfig.getStore().getId() != store.getId()){
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
            MsgVo msgVo1 = pluginsServiceImpl.checkPlugins(null,store,paymentService.getClass().getName());
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

    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    @ResponseBody
    public MsgVo delete(@PathVariable("id")Long id){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        if(store.getConfigType() == 1) {
            msgVo.setCode(40002);
            msgVo.setMsg("统一使用总部的配置");
            return msgVo;
        }
        PaymentsConfig paymentsConfig = paymentsConfigRepository.findById(id).orElse(null);
        if(paymentsConfig == null || paymentsConfig.getStore().getId() != store.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("配置不存在");
            return msgVo;
        }
        paymentsConfigRepository.delete(paymentsConfig);
        return msgVo;
    }

    @ApiOperation(value="可使用的支付列表", notes="可使用的支付列表")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "/can_used",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");
        List<String> paymentTags = new ArrayList<>();
        paymentTags.add("-1");
        PayUtil payUtil = new PayUtil();
        List<PaymentService> paymentServices = payUtil.getPaymentClass(null);
        for(PaymentService paymentService:paymentServices){
            if(paymentService.getPayOrCollect() == PayOrCollectEnum.Collect.getIndex()){
                PluginsService pluginsService = (PluginsService)paymentService;
                paymentTags.add(pluginsService.getPluginsTag());
            }
        }
        Criteria<PaymentsConfig> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("status", 2));
        criteria.add(Restrictions.in("tag",paymentTags));
        if(store.getConfigType() == 1){//使用总部的统一配置
            criteria.add(Restrictions.eq("mainStore",store.getMainStore()));
        }else{
            criteria.add(Restrictions.eq("store",store));
        }
        List<PaymentsConfig> paymentsConfigs = paymentsConfigRepository.findAll(criteria);
        msgVo.getData().put("paymentsConfigs",paymentsConfigs);
        msgVo.setMsg("获取成功");
        return msgVo;
    }
}

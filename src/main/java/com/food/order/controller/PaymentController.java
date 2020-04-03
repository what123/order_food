package com.food.order.controller;


import com.food.order.model.MsgVo;
import com.food.order.model.config.Config;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.model.service.OrdersServiceImpl;
import com.food.order.model.service.PluginsServiceImpl;
import com.food.order.plugins.PluginsData;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.PluginsUtils;
import com.food.order.plugins.pay.PayUtil;
import com.food.order.plugins.pay.PaymentService;
import com.food.order.plugins.pay.balance.Balance;
import com.food.order.plugins.printer.PrinterDataModeTypeEnum;
import com.food.order.websocket.WebSocketServer;
import com.food.order.websocket.WebsocketTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Controller("payment")
@RequestMapping("/api/payment")
@CrossOrigin
public class PaymentController extends BaseController {

    @Autowired
    OrdersRepository ordersRepository;
    @Autowired
    PaymentsConfigRepository paymentsConfigRepository;
    @Autowired
    OauthUserRepository oauthUserRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TablesRepository tablesRepository;
    @Autowired
    PluginsRepository pluginsRepository;
    @Autowired
    OrdersServiceImpl ordersServiceImpl;
    @Autowired
    PluginsServiceImpl pluginsServiceImpl;
    @Autowired
    MainStoreRepository mainStoreRepository;
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    ConsumerRepository consumerRepository;
    @Autowired
    VipUserCardRepository vipUserCardRepository;



    @RequestMapping(value = "/pay/{payment_id}",method={RequestMethod.GET})
    public String pay(HttpServletRequest request,
                            @PathVariable("payment_id")Long payment_id,
                            @RequestParam("order_id")Long order_id,
                            @RequestParam("token")String token,
                            HashMap<String, Object> map
                          ){
        if(token == null){
            map.put("msg","token不能为空,请重新登录后尝试");
            return "error";
        }
        Orders orders = ordersRepository.findById(order_id).orElse(null);

        if(orders == null){
            map.put("msg","订单不存在");
            return "error";
        }
        if(orders.getPayStatus() > 1){
            if(orders.getType() == OrderTypeEnum.GOODS_ORDERS.getIndex()) {
                //进入订单详情
                Map<String, String> params = new HashMap<>();
                params.put("order_id", "" + orders.getId());
                params.put("t", "" + System.currentTimeMillis());
                String url = getPageViewUrl(orders.getStore(), "orderDetail", params);
                return "redirect:" + url;
            }else{
                map.put("msg","已经购买过了");
                return "success";
            }
        }


        PaymentsConfig paymentsConfig = paymentsConfigRepository.findById(payment_id).orElse(null);

        if(paymentsConfig == null){
            map.put("msg","支付配置异常,请联系管理员");
            return "error";

        }
        orders.setPaymentId(payment_id);
        orders.setPayTypeStr(paymentsConfig.getName());//先设置为该支付类型

        if(orders.getRealPrice() == 0){//免单
            orders.setPayStatus(2);
            orders.setApplyStatus(2);
            orders.setPayTime(new Date());
            ordersRepository.save(orders);


            if(orders.getType() == OrderTypeEnum.GOODS_ORDERS.getIndex()) {
                //更新菜品支付状态
                Criteria<OrderGoods> orderGoodsCriteria = new Criteria<>();
                orderGoodsCriteria.add(Restrictions.eq("orders", orders));
                List<OrderGoods> orderGoodsList = orderGoodsRepository.findAll(orderGoodsCriteria);
                for(OrderGoods orderGoods1:orderGoodsList) {
                    orderGoods1.setPayStatus(1);
                    orderGoodsRepository.save(orderGoods1);
                }

                // 打印订单
                printOrderById(orders.getId(), PrinterDataModeTypeEnum.PAY_ORDER.getIndex(),false);

                Criteria<UserConfigs> userConfigsCriteria = new Criteria<>();
                userConfigsCriteria.add(Restrictions.eq("store",orders.getStore()));
                userConfigsCriteria.add(Restrictions.eq("valueKey",UserConfigTypeEnum.ORDERS_GOODS_TYPE.getIndex()));
                UserConfigs userConfigs = userSettingRepository.findOne(userConfigsCriteria).orElse(null);
                String orderGoodsType = "1";
                if(userConfigs != null){
                    orderGoodsType = userConfigs.getValueStr();
                }

                if(orders.getStore().getPayConfig() == 1 && orderGoodsType.equals("2")){//预付费且打印模式
                    printOrderGoodsByOrderId(orders.getId(),PrinterDataModeTypeEnum.ORDER_GOOD.getIndex(),false);

                    for(OrderGoods orderGoods1:orderGoodsList) {
                        orderGoods1.setMakingCount(orderGoods1.getCount());
                        orderGoods1.setMakedCount(orderGoods1.getCount());
                        orderGoodsRepository.save(orderGoods1);
                    }
                }
                //进入订单详情
                Map<String, String> params = new HashMap<>();
                params.put("order_id", "" + orders.getId());
                params.put("t", "" + System.currentTimeMillis());
                String url = getPageViewUrl(orders.getStore(), "orderDetail", params);
                return "redirect:" + url;
            }else if(orders.getType() == OrderTypeEnum.PLUGINS_ORDERS.getIndex()){//购买插件
                map.put("msg","购买成功");
                return "success";
            }else if(orders.getType() == OrderTypeEnum.CHARGE_ORDERS.getIndex()){//充值功能
                // 查找是不是有对应的消费者
                Criteria<Consumer> consumerCriteria = new Criteria<>();
                consumerCriteria.add(Restrictions.eq("user",orders.getPayUser()));
                Consumer consumer = consumerRepository.findOne(consumerCriteria).orElse(null);

                if(consumer != null && !consumer.isDelete()) {
                    Criteria<VipUserCard> vipUserCardCriteria = new Criteria<>();
                    vipUserCardCriteria.add(Restrictions.eq("consumer", consumer));

                    VipUserCard vipUserCard = vipUserCardRepository.findOne(vipUserCardCriteria).orElse(null);
                    if (vipUserCard != null) {
                        if (vipUserCard.getBalance() == null) {
                            vipUserCard.setBalance(0);
                        }
                        vipUserCard.setBalance(vipUserCard.getBalance() + orders.getRealPrice());
                        vipUserCardRepository.saveAndFlush(vipUserCard);
                    }
                }
                map.put("msg","充值成功");
                return "success";
            }
        }

        TmpCache tmpCache = tmpCacheRepository.findOneByCKey(token);
        if(tmpCache == null){
            map.put("msg","token授权失败,请重新登录后尝试");
            return "error";
        }
        User user =  userRepository.findById(Long.parseLong(tmpCache.getCValue())).orElse(null);
        if(user != null) {
            Criteria<OauthUser> criteria = new Criteria<>();
            criteria.add(Restrictions.eq("user", user));
            List<OauthUser> oauthUserList = oauthUserRepository.findAll(criteria);
            // 查找是不是有对应的消费者
            Criteria<Consumer> consumerCriteria = new Criteria<>();
            consumerCriteria.add(Restrictions.eq("user",orders.getPayUser()));
            Consumer consumer = consumerRepository.findOne(consumerCriteria).orElse(null);

            if(consumer != null && !consumer.isDelete()) {
                Criteria<VipUserCard> vipUserCardCriteria = new Criteria<>();
                vipUserCardCriteria.add(Restrictions.eq("consumer", consumer));
                VipUserCard vipUserCard = vipUserCardRepository.findOne(vipUserCardCriteria).orElse(null);
                if (vipUserCard != null) {
                    if (vipUserCard.getBalance() == null) {
                        vipUserCard.setBalance(0);
                    }
                    request.setAttribute("balance",vipUserCard.getBalance());
                }
            }


            paymentsConfig.getParamsMap().put("uid",user.getId());
        }else{
            map.put("msg","当前用户不存在,请重新登录试试");
            return "error";
        }
        orders.setPaymentId(paymentsConfig.getId());
        //余额支付失败的话，需要退还余额


        List<PaymentService> paymentServices = new PayUtil().getPaymentClass(paymentsConfig.getTag());
        if(paymentServices.size() == 0){
            map.put("msg","支付插件不存在,请检查配置");
            return "error";
        }
        PaymentService paymentService = paymentServices.get(0);

        //检测支付插件是否已经购买或过期
        MsgVo msgVo = pluginsServiceImpl.checkPlugins(orders.getMainStore(),orders.getStore(),paymentService.getClass().getName());
        if(msgVo.getCode() != 0){//过期或未购买
            map.put("msg",msgVo.getMsg());
            return "error";
        }
        paymentService.setParams(paymentsConfig,Config.host);
        PluginsData payData = paymentService.getPaySendData(request,orders.getOrderNo(),orders.getRealPrice());
        if(payData.getCode() != 200){//展示错误信息
            String msg = payData.getMsg();
            map.put("msg",msg);
            return "error";
        }

        PluginsData payData1 = paymentService.doPay(payData,map);

        if(payData1.getCode() == 200){//展示支付页面
            orders.setPayUser(user);
            ordersRepository.save(orders);//更新订单数据
            String url = (String) payData1.getData().get("url");
            return url;
        }else{
            String msg = payData1.getMsg();
            map.put("msg",msg);
            return "error";
        }

    }

    @RequestMapping(value = "/callback/{order_no}",method={RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    @Transactional
    public String callback(HttpServletRequest request,@PathVariable(value = "order_no")String order_no){
        Criteria<Orders> ordersCriteria = new Criteria<>();
        ordersCriteria.add(Restrictions.eq("orderNo",order_no));
        Orders orders = ordersRepository.findOne(ordersCriteria).orElse(null);
        if(orders == null){
            return "fail";
        }
        if(orders.getPayStatus() > 1){
            return "success";
        }

        Long payment_id = orders.getPaymentId();
        PaymentsConfig paymentsConfig = paymentsConfigRepository.findById(payment_id).orElse(null);

        if(paymentsConfig == null){
            return "fail";
        }

        Long payUid = orders.getPayUser().getId();
        Criteria<OauthUser> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("user",userRepository.findById(payUid).orElse(null)));
        List<OauthUser> oauthUserList = oauthUserRepository.findAll(criteria);
        PayUtil payUtil = new PayUtil();

        String tag = paymentsConfig.getTag();
        for(OauthUser oauthUser1:oauthUserList){
            if(payUtil.getTagsByOauthTag(oauthUser1.getTag()).contains(tag)){
                paymentsConfig.setOauthUser(oauthUser1);
                break;
            }
        }

        List<PaymentService> paymentServices = new PayUtil().getPaymentClass(tag);
        if(paymentServices.size() == 0){
            return "fail";
        }
        PaymentService paymentService = paymentServices.get(0);
        paymentService.setParams(paymentsConfig,Config.host);

        PluginsData payData = paymentService.callback(request,orders);
        if(payData.getCode() == 200){
            orders.setPayStatus(2);
            orders.setApplyStatus(2);
            if(orders.getType() == OrderTypeEnum.GOODS_ORDERS.getIndex()) {//菜品订单

                ordersRepository.save(orders);

                //更新菜品支付状态
                Criteria<OrderGoods> orderGoodsCriteria = new Criteria<>();
                orderGoodsCriteria.add(Restrictions.eq("orders", orders));
                List<OrderGoods> orderGoodsList = orderGoodsRepository.findAll(orderGoodsCriteria);
                Integer bonus = 0;
                for(OrderGoods orderGoods1:orderGoodsList) {
                    orderGoods1.setPayStatus(1);
                    orderGoodsRepository.save(orderGoods1);
                    bonus = bonus + orderGoods1.getGoods().getAddBonus();
                }

                //余额支付的话，需要扣除余额
                PluginsService pluginsService = (PluginsService)paymentService;
                if(pluginsService.getPluginsTag() == Balance.class.getName()){


                    Criteria<Consumer> consumerCriteria = new Criteria<>();
                    consumerCriteria.add(Restrictions.eq("user",orders.getPayUser()));
                    Consumer consumer = consumerRepository.findOne(consumerCriteria).orElse(null);

                    if(consumer != null && !consumer.isDelete()) {
                        Criteria<VipUserCard> vipUserCardCriteria = new Criteria<>();
                        vipUserCardCriteria.add(Restrictions.eq("consumer", consumer));

                        VipUserCard vipUserCard = vipUserCardRepository.findOne(vipUserCardCriteria).orElse(null);
                        if (vipUserCard != null) {
                            if (vipUserCard.getBalance() == null) {
                                vipUserCard.setBalance(0);
                            }
                            vipUserCard.setBalance(vipUserCard.getBalance() - orders.getRealPrice());

                            if (vipUserCard.getBonus() == null) {
                                vipUserCard.setBonus(0);
                            }
                            vipUserCard.setBonus(vipUserCard.getBonus()+bonus);//记录积分
                            vipUserCardRepository.saveAndFlush(vipUserCard);
                        }
                    }

                }

                try {//向店家推送通知
                    Map<String, Object> map = new HashMap<>();
                    map.put("order_no", orders.getOrderNo());
                    map.put("table_number", orders.getTableNumber());
                    WebSocketServer.sendInfo("" + orders.getStore().getUser().getId(), WebsocketTypeEnum.PAY_ORDER.getIndex(), map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Criteria<Tables> tablesCriteria = new Criteria<>();
                tablesCriteria.add(Restrictions.eq("number", orders.getTableNumber()));
                tablesCriteria.add(Restrictions.eq("store", orders.getStore()));
                Tables table = tablesRepository.findOne(tablesCriteria).orElse(null);
                table.setStatus(2);
                tablesRepository.save(table);
                // 打印订单

                Criteria<UserConfigs> userConfigsCriteria = new Criteria<>();
                userConfigsCriteria.add(Restrictions.eq("store",orders.getStore()));
                userConfigsCriteria.add(Restrictions.eq("valueKey",UserConfigTypeEnum.ORDERS_GOODS_TYPE.getIndex()));
                UserConfigs userConfigs = userSettingRepository.findOne(userConfigsCriteria).orElse(null);
                String orderGoodsType = "1";
                if(userConfigs != null){
                    orderGoodsType = userConfigs.getValueStr();
                }
                printOrderById(orders.getId(), PrinterDataModeTypeEnum.PAY_ORDER.getIndex(),false);
                if(orders.getStore().getPayConfig() == 1 && orderGoodsType.equals("2")){//打印模式
                    printOrderGoodsByOrderId(orders.getId(),PrinterDataModeTypeEnum.ORDER_GOOD.getIndex(),false);

                    for(OrderGoods orderGoods1:orderGoodsList) {
                        orderGoods1.setMakingCount(orderGoods1.getCount());
                        orderGoods1.setMakedCount(orderGoods1.getCount());
                        orderGoodsRepository.save(orderGoods1);
                    }
                }


            }else if(orders.getType() == OrderTypeEnum.PLUGINS_ORDERS.getIndex()){
                ordersRepository.save(orders);
                //TODO 检测增加或延长插件日期
                String tag2 = orders.getPluginsTag();
                PluginsUtils pluginsUtils = new PluginsUtils();
                List<PluginsService> pluginsServicesList = pluginsUtils.getPluginsClass();
                PluginsService pluginsService = null;
                for(PluginsService pluginsService1:pluginsServicesList){
                    if(pluginsService1.getPluginsTag().equals(tag2)){
                        pluginsService = pluginsService1;
                        break;
                    }
                }

                Criteria<Plugins> pluginsCriteria = new Criteria<>();
                if(orders.getStore() != null) {
                    pluginsCriteria.add(Restrictions.eq("store", orders.getStore()));
                }else{
                    pluginsCriteria.add(Restrictions.eq("mainStore", orders.getMainStore()));

                }
                pluginsCriteria.add(Restrictions.eq("pluginsClassPath", orders.getPluginsTag()));

                Plugins plugins = pluginsRepository.findOne(pluginsCriteria).orElse(null);

                int count = 1;
                if(plugins == null) {
                    plugins = pluginsServiceImpl.getNewPlugins(orders.getMainStore(), orders.getStore(), pluginsService);
                    count = orders.getRealPrice()/plugins.getPrice();//购买的数量
                    count -=1;//创建了一个，就是使用掉了一个了
                }else{
                    pluginsServiceImpl.setPluginPrices(orders.getMainStore(), orders.getStore(),plugins,pluginsService);
                    count = orders.getRealPrice()/plugins.getPrice();//购买的数量
                }
                plugins.setTest(false);
                if(count > 0) {//在原来的基础上再继续增加
                    Calendar calendar2 = Calendar.getInstance();
                    calendar2.setTime(plugins.getEndTime());//在原来的日期上再加天数
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    int day = pluginsService.getExpiryDay() * count;
                    if (day == 0) {//永久
                        day = 365 * 100;//100年吧
                    }

                    calendar2.add(Calendar.DATE, day);
                    String endDate = sdf2.format(calendar2.getTime());
                    try {
                        plugins.setEndTime(sdf2.parse(endDate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                pluginsRepository.save(plugins);
            }else if(orders.getType() == OrderTypeEnum.CHARGE_ORDERS.getIndex()){//充值成功，增加金额
                //TODO 增加日志


                Criteria<Consumer> consumerCriteria = new Criteria<>();
                consumerCriteria.add(Restrictions.eq("user",orders.getCreatedUser()));
                Consumer consumer = consumerRepository.findOne(consumerCriteria).orElse(null);

                if(consumer != null && !consumer.isDelete()) {
                    Criteria<VipUserCard> vipUserCardCriteria = new Criteria<>();
                    vipUserCardCriteria.add(Restrictions.eq("consumer", consumer));

                    VipUserCard vipUserCard = vipUserCardRepository.findOne(vipUserCardCriteria).orElse(null);
                    if (vipUserCard != null) {
                        if (vipUserCard.getBalance() == null) {
                            vipUserCard.setBalance(0);
                        }
                        vipUserCard.setBalance(vipUserCard.getBalance() - orders.getTotalPrice());
                        vipUserCardRepository.saveAndFlush(vipUserCard);
                    }
                }


                userRepository.save(orders.getCreatedUser());
            }
            return (String) payData.getData().get("result");
        }else{
            return (String) payData.getData().get("result");
        }
    }


    @RequestMapping(value = "/orderStatus/{order_no}",method={RequestMethod.GET})
    @ResponseBody
    public MsgVo order(HttpServletRequest request,@PathVariable(value = "order_no")Long order_no){
        MsgVo msgVo = new MsgVo();
        Orders orders = ordersRepository.findById(order_no).orElse(null);
        if(orders == null){
            msgVo.setCode(40001);
            return msgVo;
        }
        msgVo.getData().put("payStatus",orders.getPayStatus());
        return msgVo;
    }


    @RequestMapping(value = "/plugins/{uuid}",method={RequestMethod.GET})
    public String pay(HttpServletRequest request,
                      @PathVariable("uuid")String uuid,
                      @RequestParam("token")String token,
                      @RequestParam(value = "count",defaultValue = "1")int count,
                      HashMap<String, Object> map
    ){
        if(token == null){
            map.put("msg","token不能为空,请重新登录后尝试");
            return "error";
        }
        if(count < 1){
            map.put("msg","count不能小于1");
            return "error";
        }
        //默认使用payjs扫码支付
        Criteria<PaymentsConfig> paymentsConfigCriteria = new Criteria<>();
        paymentsConfigCriteria.add(Restrictions.eq("isSystem",true));
        PaymentsConfig paymentsConfig = paymentsConfigRepository.findOne(paymentsConfigCriteria).orElse(null);

        if(paymentsConfig == null){
            map.put("msg","支付配置异常,请联系管理员");
            return "error";

        }
        PluginsUtils pluginsUtils = new PluginsUtils();
        List<PluginsService> pluginsServicesList = pluginsUtils.getPluginsClass();
        PluginsService pluginsService = null;
        for(PluginsService pluginsService1:pluginsServicesList){
            if(pluginsService1.getPluginsUUID().equals(uuid)){
                pluginsService = pluginsService1;
                break;
            }
        }
        if(pluginsService == null){
            map.put("msg","插件不存在");
            return "error";

        }
        String tag = pluginsService.getPluginsTag();

        TmpCache tmpCache = tmpCacheRepository.findOneByCKey(token);
        if(tmpCache == null){
            map.put("msg","token授权失败,请重新登录后尝试");
            return "error";
        }
        User user =  userRepository.findById(Long.parseLong(tmpCache.getCValue())).orElse(null);
        Criteria<MainStore> mainStoreCriteria = new Criteria<>();
        mainStoreCriteria.add(Restrictions.eq("user",user));

        MsgVo msgVo = null;
        Store store = null;
        MainStore mainStore = mainStoreRepository.findOne(mainStoreCriteria).orElse(null);
        if(mainStore == null){
            Criteria<Store> storeCriteria = new Criteria<>();
            storeCriteria.add(Restrictions.eq("user",user));
            store = storeRepository.findOne(storeCriteria).orElse(null);
            if(store == null){
                map.put("msg","token授权失败,请重新登录后尝试");
                return "error";
            }
        }
        msgVo = pluginsServiceImpl.checkPlugins(mainStore,store,tag);
        if(msgVo.getCode() == 60001 || msgVo.getCode() == 60002){
            map.put("msg",msgVo.getMsg());
            return "error";
        }
        Integer price = (Integer) msgVo.getData().get("price") * count;
        if(price == 0){//免费插件

            Criteria<Plugins> pluginsCriteria = new Criteria<>();
            if(store != null) {
                pluginsCriteria.add(Restrictions.eq("store", store));
            }else{
                pluginsCriteria.add(Restrictions.eq("mainStore", mainStore));

            }
            pluginsCriteria.add(Restrictions.eq("pluginsClassPath", tag));

            int day = pluginsService.getExpiryDay() * count;
            if(day == 0){//永久
                day = 365*100;//100年吧
            }
            Plugins plugins = pluginsRepository.findOne(pluginsCriteria).orElse(null);
            if(plugins == null) {
                plugins = new Plugins();
                Calendar calendar2 = Calendar.getInstance();
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                calendar2.add(Calendar.DATE, day);
                String endDate = sdf2.format(calendar2.getTime());
                plugins.setStartTime(new Date());
                plugins.setPicPath(pluginsService.getPicPath());
                plugins.setType(pluginsService.getPluginsType());
                try {
                    plugins.setEndTime(sdf2.parse(endDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else{
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(plugins.getEndTime());//在原来的日期上再加天数
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                calendar2.add(Calendar.DATE, day);
                String endDate = sdf2.format(calendar2.getTime());
                try {
                    plugins.setEndTime(sdf2.parse(endDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            plugins.setName(pluginsService.getName());
            plugins.setStore(store);
            plugins.setMainStore(mainStore);
            plugins.setPluginsClassPath(tag);
            plugins.setUuid(pluginsService.getPluginsUUID());
            pluginsRepository.save(plugins);
            // 购买成功页面
            map.put("msg","购买成功");
            return "success";
        }

        // 创建购买订单
        Orders orders = new Orders();
        orders.setType(OrderTypeEnum.PLUGINS_ORDERS.getIndex());
        orders.setApplyStatus(2);
        orders.setRealPrice(price);
        orders.setTotalPrice(price);
        orders.setPluginsTag(tag);
        orders.setCreatedUser(user);
        orders.setStore(store);
        orders.setMainStore(mainStore);
        orders.setPaymentId(paymentsConfig.getId());
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMddHHmmss");
        String outTradeNo = user.getId()+""+simpleDateFormat1.format(new Date())+((int)(10+Math.random()*(99-10+1)));//MD5Util.MD5Encode(goodsList.get(0).getId()+"-"+user_id+"-"+System.currentTimeMillis());
        orders.setOrderNo(outTradeNo);
        orders = ordersRepository.saveAndFlush(orders);
        // 跳到支付页面
        return "redirect:"+Config.host+"/api/payment/pay/"+paymentsConfig.getId()+"?order_id="+orders.getId()+"&token="+token;
    }


}

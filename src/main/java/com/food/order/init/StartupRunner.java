package com.food.order.init;

/**
 * Created by Administrator on 2017/11/8.
 */

import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.model.service.SystemActiveServiceImpl;
import com.food.order.plugins.PluginsData;
import com.food.order.plugins.printer.PrinterService;
import com.food.order.plugins.printer.PrinterUtil;
import com.food.order.utils.utils.rsa.RSA;
import com.food.order.websocket.WebSocketServer;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 服务启动执行
 *
 */
@Component
@Order(value=1)
public class StartupRunner implements CommandLineRunner {
    @Autowired
    SuperAdminRepository superAdminRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PrinterLogsRepository printerLogsRepository;

    @Autowired
    SystemActiveServiceImpl systemActiveServiceImpl;


    @Override
    public void run(String... args) throws Exception {
        initActive();
        addSuperAdmin();
//        addDefaultPageView();
//        initPluginsPrices();
        startPrinterTask();
        cleanWebSocketTask();
    }
    private void initActive(){//系统激活
        systemActiveServiceImpl.getActiveInfo();
    }

    //初始化超管
    private void addSuperAdmin(){
        //增加超管
        List<SuperAdmin> superAdmins = superAdminRepository.findAll();
        if(superAdmins == null || superAdmins.size() == 0){
            User user = new User();
            user.setAccount("admin");
            user.setPassword(user.getPwd("888888"));
            user.setBelong(1);
            user = userRepository.saveAndFlush(user);

            SuperAdmin superAdmin = new SuperAdmin();
            superAdmin.setName("平台超级管理员");
            superAdmin.setUser(user);
            superAdminRepository.saveAndFlush(superAdmin);
        }
    }
    //初始化点餐默认页面
//    private void addDefaultPageView(){
//
//        Criteria<PageView> criteria = new Criteria<>();
//        criteria.add(Restrictions.eq("viewPath","default"));//默认的
//        PageView pageView = pageViewRepository.findOne(criteria).orElse(null);
//        if(pageView == null){
//            pageView = new PageView();
//            pageView.setName("默认");
//            pageView.setViewPath("default");
//            pageView.setStatus(2);
//            //TODO 图片初始化
//
//            pageViewRepository.saveAndFlush(pageView);
//        }
//    }

    //打印机任务
    private void startPrinterTask(){
        new Thread(){
            @Override
            public void run() {
                boolean isRunning = true;
                while (isRunning){
                    Criteria<PrinterLogs> criteria = new Criteria<>();
                    criteria.add(Restrictions.eq("status",1));//未推送的
                    List<PrinterLogs> printerLogs = printerLogsRepository.findAll(criteria);
                    if(printerLogs.size() > 0) {
                        for (PrinterLogs printerLog : printerLogs) {
                            Printer printer = printerLog.getPrinter();
                            List<PrinterService> printerServices = new PrinterUtil().getPrinterClass(printer.getApiTag());
                            if(printerServices.size() == 0){//没有打印机配置
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                continue;
                            }
                            printerLog.setStatus(2);
                            PrinterService printerService = printerServices.get(0);
                            printer.setPrintData(printerLog.getPrintData());
                            printer.setOrigin_id(""+printerLog.getId());
                            PluginsData printerData = printerService.printData(printer);
                            printerLog.setPrinterApiResult((String)printerData.getData().get("result"));
                            if(printerData.getCode() == 200){
                                printerLog.setStatus(3);
                            }else{
                                printerLog.setStatus(4);
                            }
                            printerLogsRepository.save(printerLog);
                        }
                    }else{
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                super.run();
            }
        }.start();
    }

    /**
     * 清理websocket僵尸用户
     */
    private void cleanWebSocketTask(){

        new Thread(){
            @Override
            public void run() {
                boolean isRunning = true;
                while (isRunning){
                    
                    if(WebSocketServer.authLongTime.size() > 0) {
                        //遍历map中的键
                        List<String> delKeys = new ArrayList<>();
                        for (String key : WebSocketServer.authLongTime.keySet()) {
                            if(!WebSocketServer.checkAuth(key)){
                                delKeys.add(key);
                            }
                        }
                        if(delKeys.size() > 0){
                            for(String key:delKeys){
                                WebSocketServer.authLongTime.remove(key);
                            }
                        }
                        try {
                            Thread.sleep(60000*5);//5分钟清理一次
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else{
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                super.run();
            }
        }.start();
    }

    //初始化插件价格表
//    private void initPluginsPrices(){
//        List<String> pluginsClassPaths = new ArrayList<>();
//        //授权插件
//        Map<String,OauthService> oauthServiceMap = new HashMap<>();
//        OauthUtil oauthUtil = new OauthUtil();
//        List<OauthService> oauthServices = oauthUtil.getOauthClass(0);
//        for(OauthService oauthService:oauthServices){
//            pluginsClassPaths.add(oauthService.getClass().getName());
//            oauthServiceMap.put(oauthService.getClass().getName(),oauthService);
//        }
//        //支付插件
//        PayUtil payUtil = new PayUtil();
//        Map<String,PaymentService> paymentServiceHashMap = new HashMap<>();
//        List<PaymentService> paymentServices = payUtil.getPaymentClass(0);
//        for(PaymentService paymentService:paymentServices){
//            pluginsClassPaths.add(paymentService.getClass().getName());
//            paymentServiceHashMap.put(paymentService.getClass().getName(),paymentService);
//        }
//        //TODO 打印机插件,暂时不把打印机插件纳入
//
//        //点餐主题插件
//        Criteria<PageView> pageViewCriteria = new Criteria<>();
//        pageViewCriteria.add(Restrictions.eq("isSystem", 1));
//        List<PageView> pageViews = pageViewRepository.findAll(pageViewCriteria);
//        Map<String,PageView> pageViewMap = new HashMap<>();
//        for(PageView pageView:pageViews) {
//            pageViewMap.put(pageView.getViewPath(), pageView);
//            pluginsClassPaths.add(pageView.getViewPath());
//        }
//
//
//        Criteria<PluginsPrices> pluginsCriteria = new Criteria<>();
//        pluginsCriteria.add(Restrictions.in("pluginsClassPath", pluginsClassPaths));
//        List<PluginsPrices> prices = pluginsPricesRepository.findAll(pluginsCriteria);
//        List<String> hasAddPaths = new ArrayList<>();//已经存在的插件
//        if(prices.size() > 0) {
//            for (PluginsPrices Price : prices) {
//                hasAddPaths.add(Price.getPluginsClassPath());
//            }
//        }
//        //将未加入数据库的插件加入到数据库中
//        for(String path:pluginsClassPaths){
//            if(!hasAddPaths.contains(path)){//库中不存在
//                if(oauthServiceMap.containsKey(path)){
//                    PluginsPrices pluginsPrices1 = new PluginsPrices();
//                    pluginsPrices1.setType(oauthServiceMap.get(path).getPluginsType());
//                    pluginsPrices1.setPluginsClassPath(path);
//                    pluginsPrices1.setName(OauthTypeEnum.getName(oauthServiceMap.get(path).getOauthType()));
//                    pluginsPricesRepository.save(pluginsPrices1);
//                }else if(paymentServiceHashMap.containsKey(path)){
//                    PluginsPrices pluginsPrices1 = new PluginsPrices();
//                    pluginsPrices1.setType(paymentServiceHashMap.get(path).getPluginsType());
//                    pluginsPrices1.setPluginsClassPath(path);
//                    pluginsPrices1.setName(PaymentTypeEnum.getName(paymentServiceHashMap.get(path).getPaymentType()));
//                    pluginsPricesRepository.save(pluginsPrices1);
//                }else if(pageViewMap.containsKey(path)){
//                    PluginsPrices pluginsPrices1 = new PluginsPrices();
//                    pluginsPrices1.setType(PluginsTypeEnum.PAGE_VIEW_PLUGINS.getIndex());
//                    pluginsPrices1.setPluginsClassPath(path);
//                    pluginsPrices1.setName(pageViewMap.get(path).getName());
//                    pluginsPricesRepository.save(pluginsPrices1);
//                }
//            }
//        }
//
//    }

}
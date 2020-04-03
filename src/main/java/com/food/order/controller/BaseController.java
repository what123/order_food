package com.food.order.controller;

import com.alibaba.fastjson.JSON;
import com.food.order.model.MsgVo;
import com.food.order.model.config.Config;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.model.service.PluginsServiceImpl;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.PluginsTypeEnum;
import com.food.order.plugins.pageView.PageViewService;
import com.food.order.plugins.printer.PrinterDataModeTypeEnum;
import com.food.order.plugins.printer.PrinterService;
import com.food.order.plugins.printer.PrinterUtil;
import com.food.order.plugins.printer.feie.BaseFeie;
import com.food.order.utils.utils.MD5Util;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@CrossOrigin
public class BaseController {

    @Autowired
    protected HttpServletRequest request;
    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected TmpCacheRepository tmpCacheRepository;

    @Autowired
    protected StoreLogRepository storeLogRepository;

    @Autowired
    protected OrderGoodsRepository orderGoodsRepository;

    @Autowired
    protected OrdersRepository ordersRepository;

    @Autowired
    protected PrinterRepository printerRepository;

    @Autowired
    protected PrinterLogsRepository printerLogsRepository;

    @Autowired
    UserSettingRepository userSettingRepository;


    @Autowired
    protected StoreRepository storeRepository;

    @Autowired
    PluginsRepository pluginsRepository;

    @Autowired
    PluginsServiceImpl pluginsServiceImpl;




    protected boolean saveToken(String tag, User user, int days){
        String token = null;
        try {
            token = MD5Util.MD5Encode(tag+"_"+user.getAccount()+"#@#"+user.getId()+"#@#"+System.currentTimeMillis());
            TmpCache tmpCache = new TmpCache();
            tmpCache.setCValue(""+user.getId());
            tmpCache.setCKey(token);
            if(days == 0){
                days = tmpCache.getExpireTime();
            }else{
                days = 3600*1000*24*days;
            }
            tmpCache.setEndTime(tmpCache.getCreateTime().getTime()+ days);//管理员默认7天有效登录
            tmpCacheRepository.save(tmpCache);
            user.setAccessToken(token);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    protected boolean removeToken(User user){
        try {
            if(user.getAccessToken() != null) {
                TmpCache tmpCache = tmpCacheRepository.findOneByCKey(user.getAccessToken());
                if(tmpCache != null){
                    tmpCacheRepository.delete(tmpCache);
                }
                user.setAccessToken(null);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    protected void saveStoreLog(Store stroe, StoreUser storeUser, String note){
        StoreLog storeLog = new StoreLog();
        storeLog.setNote(note);
        storeLog.setStore(stroe);
        storeLog.setStoreUser(storeUser);
        storeLogRepository.save(storeLog);
    }

//    protected String refund(Orders orders,Integer refundPrice){
//        if(orders.getPayStatus() == 2 && (orders.getPayType() == 1 || orders.getPayType() == 2 || orders.getPayType() == 4)){
//            //TODO 退款对应的菜品金额
//            return "success";
//        }else{
//            return "请在线下退款给客户";
//        }
//    }

    /**
     * 更新订单价格
     * @param order
     * @param couponPrice
     */
    protected Integer updateOrderPrice(Orders order,Integer couponPrice){
        Integer realPrice = order.getRealPrice();
        Criteria<OrderGoods> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("orders",order));
        List<OrderGoods> orderGoodsList = orderGoodsRepository.findAll(criteria);
        Integer totalPrice = 0;
        for(OrderGoods orderGoods:orderGoodsList){
            Goods goods = JSON.parseObject(orderGoods.getGoodsStr(), Goods.class);
            Integer totalGoodsPrice = goods.getSell_price()*orderGoods.getCount();
            orderGoods.setTotalPrice(totalGoodsPrice);
            orderGoods.setRealPrice(totalGoodsPrice);
            totalPrice = totalPrice + totalGoodsPrice;
        }
        orderGoodsRepository.saveAll(orderGoodsList);//更新一下价格
        order.setTotalPrice(totalPrice);
        order.setRealPrice(totalPrice-couponPrice);//去掉优惠的钱
        ordersRepository.save(order);

        Integer refundPrice = 0;
        if(realPrice != null){
            refundPrice = realPrice - order.getRealPrice();
        }

        return refundPrice;
    }

    /**
     * 打印订单
     * @param type
     * @return
     */
    protected MsgVo printOrderById(Long order_id,int type,boolean isAdd){
        MsgVo msgVo = new MsgVo();
        Orders orders = ordersRepository.findById(order_id).orElse(null);
        Criteria<OrderGoods> orderGoodsCriteria = new Criteria<>();
        orderGoodsCriteria.add(Restrictions.eq("orders", orders));
        List<OrderGoods> orderGoodsList = orderGoodsRepository.findAll(orderGoodsCriteria);
        if(orderGoodsList == null || orderGoodsList.size() == 0){
            msgVo.setCode(40003);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }

        Criteria<Printer> printerCriteria = new Criteria<>();
        printerCriteria.add(Restrictions.eq("store", orders.getStore()));
        printerCriteria.add(Restrictions.eq("status", 2));
        List<Printer> printers = printerRepository.findAll(printerCriteria);

        PrinterDataModel printerDataModel = null;
        Map<String,Printer> surePrnters = new HashMap();//用于打印的数据
        for (Printer printer:printers) {
            List<PrinterDataModel> printerDataModels = printer.getPrinterDataModels();
            if(printerDataModels.size() > 0){
                for (PrinterDataModel printerDataModel1:printerDataModels){
                    if(printerDataModel1.getType() == type){
                        String data = printerDataModel1.getPrintData();//模板
                        if(data != null && !data.trim().equals("")) {
                            data = new PrinterUtil().prasePrintData(data, orders, orderGoodsList,isAdd);//转换订单数据
                            printer.setPrintData(data);
                            surePrnters.put(printer.getApiTag(), printer);
                            printerDataModel = printerDataModel1;
                        }
                    }
                }
            }
        }
        if(surePrnters.size() == 0){
            msgVo.setCode(40008);
            msgVo.setMsg("没有对应的打印机");
            return msgVo;
        }

        List<PrinterService> printerServices = new com.food.order.plugins.printer.PrinterUtil().getPrinterClass(null);//打印机插件
        if(printerServices.size() == 0){
            msgVo.setCode(40009);
            msgVo.setMsg("未找到对应类型的打印机");
            return msgVo;
        }
        Store store  = orders.getStore();
        for(PrinterService printerService : printerServices) { //加入打印队列
            PluginsService pluginsService = (PluginsService)printerService;
            if(surePrnters.containsKey(pluginsService.getPluginsTag())){
                Printer printer = surePrnters.get(pluginsService.getPluginsTag());
                PrinterLogs printerLogs = new PrinterLogs();
                printerLogs.setPrinter(printer);
                printerLogs.setPrinterDataModel(printerDataModel);
                printerLogs.setOrders(orders);
                String data = printerService.getPrintData(printer.getPrintData());
                printerLogs.setPrintData(data);
                printerLogs.setStore(store);
                printerLogsRepository.save(printerLogs);
            }
        }


        return msgVo;
    }
    /**
     * 打印订单（指定打印机）
     * @param type
     * @return
     */
    protected MsgVo printOrderById(Store store,Long printer_id,Long order_id,int type){
        MsgVo msgVo = new MsgVo();
        Orders orders = ordersRepository.findById(order_id).orElse(null);
        if(orders == null || orders.getStore().getId() != store.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }
        Criteria<OrderGoods> orderGoodsCriteria = new Criteria<>();
        orderGoodsCriteria.add(Restrictions.eq("orders", orders));
        List<OrderGoods> orderGoodsList = orderGoodsRepository.findAll(orderGoodsCriteria);
        if(orderGoodsList == null || orderGoodsList.size() == 0){
            msgVo.setCode(40003);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }

        Criteria<Printer> printerCriteria = new Criteria<>();
        printerCriteria.add(Restrictions.eq("store", store));
        printerCriteria.add(Restrictions.eq("status", 2));
        printerCriteria.add(Restrictions.eq("id", printer_id));

        List<Printer> printers = printerRepository.findAll(printerCriteria);

        if(printers == null || printers.size() == 0){
            msgVo.setCode(40004);
            msgVo.setMsg("打印机不存在");
            return msgVo;
        }

        PrinterDataModel printerDataModel = null;
        Map<String,Printer> surePrnters = new HashMap();//用于打印的数据
        for (Printer printer:printers) {
            List<PrinterDataModel> printerDataModels = printer.getPrinterDataModels();
            if(printerDataModels.size() > 0){
                for (PrinterDataModel printerDataModel1:printerDataModels){
                    if(printerDataModel1.getType() == type){
                        String data = printerDataModel1.getPrintData();//模板
                        data = new PrinterUtil().prasePrintData(data,orders,orderGoodsList,false);//转换订单数据
                        printer.setPrintData(data);
                        surePrnters.put(printer.getApiTag(),printer);
                        printerDataModel = printerDataModel1;
                    }
                }
            }
        }
        if(surePrnters.size() == 0){
            msgVo.setCode(40005);
            msgVo.setMsg("没有可用的打印机");
            return msgVo;
        }

        List<PrinterService> printerServices = new com.food.order.plugins.printer.PrinterUtil().getPrinterClass(null);//打印机插件
        if(printerServices.size() == 0){
            msgVo.setCode(40006);
            msgVo.setMsg("打印机插件不存在");
            return msgVo;
        }
        for(PrinterService printerService : printerServices) { //加入打印队列

            PluginsService pluginsService = (PluginsService)printerService;
            if(surePrnters.containsKey(pluginsService.getPluginsTag())){
                Printer printer = surePrnters.get(pluginsService.getPluginsTag());
                PrinterLogs printerLogs = new PrinterLogs();
                printerLogs.setPrinter(printer);
                printerLogs.setPrinterDataModel(printerDataModel);
                printerLogs.setOrders(orders);
                String data = printerService.getPrintData(printer.getPrintData());
                printerLogs.setPrintData(data);
                printerLogs.setStore(store);
                printerLogsRepository.save(printerLogs);
            }
        }

        return msgVo;
    }

    /**
     * 打印订单菜品队列
     * @return
     */
    protected MsgVo printOrderGoodsByOrderId(Long order_id,int type,boolean isAdd){
        MsgVo msgVo = new MsgVo();
        Orders orders = ordersRepository.findById(order_id).orElse(null);
        Criteria<OrderGoods> orderGoodsCriteria = new Criteria<>();
        orderGoodsCriteria.add(Restrictions.eq("orders", orders));
        List<OrderGoods> orderGoodsList = orderGoodsRepository.findAll(orderGoodsCriteria);
        if(orderGoodsList == null || orderGoodsList.size() == 0){
            msgVo.setCode(40003);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }
        for (OrderGoods orderGoods:orderGoodsList){
            printOrderGoodsById(orderGoods.getId(), type,isAdd);
        }
        return msgVo;
    }
    /**
     * 打印订单单个菜品队列
     * @return
     */
    protected MsgVo printOrderGoodsById(Long order_goods_id,int type,boolean isAdd){
        MsgVo msgVo = new MsgVo();
        OrderGoods orderGood = orderGoodsRepository.findById(order_goods_id).orElse(null);
        if(orderGood == null){
            msgVo.setCode(40003);
            msgVo.setMsg("订单菜品不存在");
            return msgVo;
        }
        Orders orders = orderGood.getOrders();
        List<OrderGoods> orderGoodsList =  new ArrayList<>();
        orderGoodsList.add(orderGood);

        Criteria<Printer> printerCriteria = new Criteria<>();
        printerCriteria.add(Restrictions.eq("store", orderGood.getOrders().getStore()));
        printerCriteria.add(Restrictions.eq("status", 2));

        if(type == PrinterDataModeTypeEnum.ORDER_GOOD.getIndex() || type == PrinterDataModeTypeEnum.MAKED.getIndex()) {//厨房打印时，要看是否有指定打印机
            if(orderGood.getGoods().getKitchenPrinter() == 0){
                msgVo.setMsg("本菜品不进行厨房打印");
                return msgVo;
            }
            List<Printer> zhidingPrinters = orderGood.getGoods().getPrinters();//指定了打印机
            if (zhidingPrinters.size() > 0) {
                List<Long> printerIds = new ArrayList<>();
                for (Printer printer : zhidingPrinters) {
                    printerIds.add(printer.getId());
                }
                printerCriteria.add(Restrictions.in("id", printerIds));
            }
        }

        List<Printer> printers = printerRepository.findAll(printerCriteria);

        PrinterDataModel printerDataModel = null;
        Map<String,Printer> surePrnters = new HashMap();//用于打印的数据
        for (Printer printer:printers) {
            List<PrinterDataModel> printerDataModels = printer.getPrinterDataModels();
            if(printerDataModels.size() > 0){
                for (PrinterDataModel printerDataModel1:printerDataModels){
                    if(printerDataModel1.getType() == type){
                        String data = printerDataModel1.getPrintData();//模板
                        data = new PrinterUtil().prasePrintData(data,orders,orderGoodsList,isAdd);//转换订单数据
                        printer.setPrintData(data);
                        surePrnters.put(printer.getApiTag(),printer);
                        printerDataModel = printerDataModel1;
                    }
                }
            }
        }
        if(surePrnters.size() == 0){
            msgVo.setCode(40008);
            msgVo.setMsg("没有对应的打印机");
            return msgVo;
        }

        List<PrinterService> printerServices = new com.food.order.plugins.printer.PrinterUtil().getPrinterClass(null);//打印机插件
        if(printerServices.size() == 0){
            msgVo.setCode(40009);
            msgVo.setMsg("未找到对应类型的打印机");
            return msgVo;
        }
        Criteria<UserConfigs> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",orders.getStore()));
        criteria.add(Restrictions.eq("valueKey",UserConfigTypeEnum.ORDERS_GOODS_TYPE.getIndex()));
        UserConfigs userConfigs = userSettingRepository.findOne(criteria).orElse(null);
        String orderGoodsType = "1";
        if(userConfigs != null){
            orderGoodsType = userConfigs.getValueStr();
        }
        Store store  = orders.getStore();
        for(PrinterService printerService : printerServices) { //加入打印队列

            PluginsService pluginsService = (PluginsService)printerService;
            if(surePrnters.containsKey(pluginsService.getPluginsTag())){
                Printer printer = surePrnters.get(pluginsService.getPluginsTag());

                if(orderGoodsType.equals("1") && printerDataModel.getType() == PrinterDataModeTypeEnum.ORDER_GOOD.getIndex()){//电脑模式，不直接进行出菜打印
                    continue;
                }
                PrinterLogs printerLogs = new PrinterLogs();
                printerLogs.setPrinter(printer);
                printerLogs.setPrinterDataModel(printerDataModel);
                printerLogs.setOrders(orders);
                String data = printerService.getPrintData(printer.getPrintData());
                printerLogs.setPrintData(data);
                printerLogs.setStore(store);
                printerLogsRepository.save(printerLogs);
            }
        }

        return msgVo;
    }

    protected String getPageViewUrl(Store store,String path,Map<String,String> params){
        String paramsStr = null;
        try {
            paramsStr = createLinkStringByGet(params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //检查是否有指定的页面
        Criteria<Plugins> consumerViewCriteria = new Criteria<>();
        if(store.getConfigType() == 1){
            if(store.getMainStore().getPageViewId() == null){
                return Config.host+"/default/index.html/#/"+path+"?"+paramsStr;//跳转到默认授权页面
            }
            consumerViewCriteria.add(Restrictions.eq("mainStore", store.getMainStore()));
            consumerViewCriteria.add(Restrictions.eq("id", store.getMainStore().getPageViewId()));
        }else {
            if(store.getPageViewId() == null){
                return Config.host+"/default/index.html/#/"+path+"?"+paramsStr;//跳转到默认授权页面
            }
            consumerViewCriteria.add(Restrictions.or(Restrictions.eq("store", store),Restrictions.eq("mainStore", store.getMainStore())));
            consumerViewCriteria.add(Restrictions.eq("id", store.getPageViewId()));
        }
        consumerViewCriteria.add(Restrictions.eq("type", PluginsTypeEnum.PAGE_VIEW_PLUGINS.getIndex()));


        Plugins pageView = pluginsRepository.findOne(consumerViewCriteria).orElse(null);
        if(pageView == null){//走默认页面
            return Config.host+"/default/index.html/#/"+path+"?"+paramsStr;//跳转到默认授权页面
        }

        //检测插件是否已经购买或过期
        MsgVo msgVo = pluginsServiceImpl.checkPlugins(null,store,pageView.getPluginsClassPath());
        if(msgVo.getCode() != 0){//过期或未购买
            return Config.host+"/default/index.html/#/"+path+"?"+paramsStr;//跳转到默认授权页面
        }

        PluginsService pluginsService = (PluginsService)msgVo.getData().get("pluginsService");

        if(pluginsService == null){//没有这个插件类
            return Config.host+"/default/index.html/#/"+path+"?"+paramsStr;//跳转到默认授权页面
        }
        PageViewService pageViewClass = (PageViewService)pluginsService;
        return Config.host+"/"+pageViewClass.getViewPath()+"/index.html/#/"+path+"?"+paramsStr;

    }
    public String createLinkStringByGet(Map<String, String> params) throws UnsupportedEncodingException {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            value = URLEncoder.encode(value, "UTF-8");
            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                 prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }
}

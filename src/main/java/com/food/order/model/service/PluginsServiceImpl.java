package com.food.order.model.service;

import com.alibaba.fastjson.JSON;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.MainStore;
import com.food.order.model.entity.Plugins;
import com.food.order.model.entity.Store;
import com.food.order.model.repository.PluginsRepository;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.PluginsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PluginsServiceImpl {
    @Autowired
    PluginsRepository pluginsRepository;

    public MsgVo checkPlugins(MainStore mainStore, Store store, String pluginsClassPath){
        MsgVo msgVo = new MsgVo();
        PluginsUtils pluginsUtils = new PluginsUtils();
        List<PluginsService> pluginsServicesList = pluginsUtils.getPluginsClass();
        PluginsService pluginsService = null;
        for(PluginsService pluginsService1:pluginsServicesList){
            if(pluginsService1.getPluginsTag().equals(pluginsClassPath)){
                pluginsService = pluginsService1;
                break;
            }
        }
        if(pluginsService == null){
            msgVo.setCode(60001);
            msgVo.setMsg("该插件未正式上线,请稍后再试");
            return msgVo;
        }
        Integer price = 0;

        int vip = 0;
        if(store != null){
            vip = store.getMainStore().getVip();
        }else{
            vip = mainStore.getVip();
        }

        if(vip == 0){
            price = pluginsService.getVip0Price();
        }else if(vip == 1){
            price = pluginsService.getVip1Price();
        }else if(vip == 2){
            price = pluginsService.getVip2Price();
        }
        if(price == -1) {//初始化数据
            msgVo.setCode(60002);
            msgVo.setMsg("该插件未正式上线,请稍后再试");
            msgVo.getData().put("pluginsService", pluginsService);
            msgVo.getData().put("price", price);
            return msgVo;
        }
        if(price > 0) {
            Criteria<Plugins> pluginsCriteria = new Criteria<>();
            if(store != null) {
                pluginsCriteria.add(Restrictions.or(Restrictions.eq("store", store), Restrictions.eq("mainStore", store.getMainStore())));
            }else{
                pluginsCriteria.add(Restrictions.eq("mainStore", mainStore));

            }
            pluginsCriteria.add(Restrictions.eq("pluginsClassPath", pluginsClassPath));

            List<Plugins> plugins = pluginsRepository.findAll(pluginsCriteria);
            if (plugins == null || plugins.size() == 0) {
                msgVo.setCode(60003);
                msgVo.setMsg("该插件需要购买才能使用");
                msgVo.getData().put("pluginsService", pluginsService);
                msgVo.getData().put("price", price);
                return msgVo;
            }
            long nowTimeStamp = System.currentTimeMillis();
            for (Plugins plugins1 : plugins) {
                if (plugins1.getStartTime().getTime() > nowTimeStamp) {
                    msgVo.setCode(60004);
                    msgVo.setMsg(plugins1.getName()+"插件未到使用时间,暂时不可用");
                    msgVo.getData().put("pluginsService", pluginsService);
                    return msgVo;
                }
                if (plugins1.getEndTime().getTime() < nowTimeStamp) {
                    msgVo.setCode(60005);
                    msgVo.setMsg(plugins1.getName()+"插件已过期，请重新购买");
                    msgVo.getData().put("pluginsService", pluginsService);
                    msgVo.getData().put("price", price);
                    return msgVo;
                }
            }

            msgVo.getData().put("plugins", plugins);
        }
        msgVo.getData().put("pluginsService", pluginsService);
        msgVo.getData().put("price", price);
        return msgVo;
    }

    public List<Plugins> checkOnline(MainStore mainStore, Store store,List<PluginsService> pluginsServices){
        List<String> pluginTags = new ArrayList<>();
        for(PluginsService pluginsService:pluginsServices){
            String pluginsClassPath = pluginsService.getPluginsTag();
            if(checkPlugins(mainStore,store, pluginsClassPath).getCode() == 0) {
                pluginTags.add(pluginsService.getPluginsTag());
            }
        }
        Criteria<Plugins> pluginsCriteria = new Criteria<>();
        pluginsCriteria.add(Restrictions.in("pluginsClassPath", pluginTags));
        pluginsCriteria.add(Restrictions.eq("isUsed", true));
        if(store != null) {
            pluginsCriteria.add(Restrictions.or(Restrictions.eq("store", store), Restrictions.eq("mainStore", store.getMainStore())));
        }else{
            pluginsCriteria.add(Restrictions.eq("mainStore", mainStore));

        }
        List<Plugins> plugins = pluginsRepository.findAll(pluginsCriteria);
        return plugins;
    }

    /**
     * 获取分店所有的插件（包括父级的）
     * @param store
     * @return
     */
    public List<Plugins> getStoreAllPlugins(Store store, int pluginsType){
        List<Plugins> allPlugins = new ArrayList<>();
        //先查自己的
        Criteria<Plugins> pluginsCriteria = new Criteria<>();
        pluginsCriteria.add(Restrictions.eq("store", store));
        if(pluginsType > 0) {
            pluginsCriteria.add(Restrictions.eq("type", pluginsType));
        }
        List<Plugins> plugins1 = pluginsRepository.findAll(pluginsCriteria);
        List<String> pluginsClassPaths = new ArrayList<>();
        if(plugins1.size()  >  0){
            for(Plugins plugins:plugins1){
                pluginsClassPaths.add(plugins.getPluginsClassPath());
            }
        }
        //再查父级的
        Criteria<Plugins> pluginsCriteria1 = new Criteria<>();
        pluginsCriteria1.add(Restrictions.eq("mainStore", store.getMainStore()));
        if(pluginsType > 0) {
            pluginsCriteria1.add(Restrictions.eq("type", pluginsType));
        }
        if(pluginsClassPaths.size() > 0) {
            pluginsCriteria1.add(Restrictions.notIn("pluginsClassPath", pluginsClassPaths));
        }
        List<Plugins> plugins2 = pluginsRepository.findAll(pluginsCriteria1);
        allPlugins.addAll(plugins1);
        allPlugins.addAll(plugins2);
        return allPlugins;
    }

    /**
     * 分店的插件
     * @param mainStore
     * @param pluginsType
     * @return
     */
    public List<Plugins> getMainStoreAllPlugins(MainStore mainStore, int pluginsType){
        Criteria<Plugins> pluginsCriteria1 = new Criteria<>();
        pluginsCriteria1.add(Restrictions.eq("mainStore", mainStore));
        if(pluginsType > 0) {
            pluginsCriteria1.add(Restrictions.eq("type", pluginsType));
        }
        List<Plugins> allPlugins = pluginsRepository.findAll(pluginsCriteria1);
        return allPlugins;
    }

    public Plugins getNewPlugins(MainStore mainStore,Store store,PluginsService pluginsService){
        Plugins plugins1 = new Plugins();
        plugins1.setId(0l);
        plugins1.setType(pluginsService.getPluginsType());
        plugins1.setName(pluginsService.getName());
        plugins1.setMainStore(mainStore);
        plugins1.setStore(store);
        plugins1.setNote(pluginsService.getNote());
        plugins1.setPluginsClassPath(pluginsService.getPluginsTag());
        plugins1.setUuid(pluginsService.getPluginsUUID());
        plugins1.setPicPath(pluginsService.getPicPath());


        int day = pluginsService.getExpiryDay();
        if(day == 0){//永久
            day = 365*100;//100年吧
        }else if(pluginsService.getFreeExpiryDay() > 0){//有试用天数
            day  = pluginsService.getFreeExpiryDay();
            plugins1.setTest(true);
        }
        Calendar calendar2 = Calendar.getInstance();
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        calendar2.add(Calendar.DATE, day);
        String endDate = sdf2.format(calendar2.getTime());
        plugins1.setStartTime(new Date());
        plugins1.setPicPath(pluginsService.getPicPath());
        plugins1.setType(pluginsService.getPluginsType());

        try {
            plugins1.setEndTime(sdf2.parse(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        setPluginPrices(mainStore,store,plugins1,pluginsService);

        Map<String, String> map = pluginsService.getParamsConfig();
        if(map != null) {
            Map<String, Map<String, Object>> map2 = new HashMap<>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                Map<String, Object> values = new HashMap<>();
                String key = entry.getKey();
                if (entry.getKey().startsWith("UPLOADFILE_")) {//上传文件
                    values.put("ele_type", "file");
                    key = key.substring(11);
                }else if (entry.getKey().startsWith("SWITCH_")) {//上传文件
                    values.put("ele_type", "switch");
                    key = key.substring(7);
                } else {//普通类型
                    values.put("ele_type", "text");
                }

                values.put("value", "");
                values.put("placeholder", entry.getValue());
                map2.put(key, values);
            }

            plugins1.setParamsStr(JSON.toJSONString(map2));
        }


        if(plugins1.getPrice() == 0 || pluginsService.getFreeExpiryDay() > 0){//免费的可以直接保存

            plugins1 = pluginsRepository.saveAndFlush(plugins1);
        }
        return plugins1;
    }
//    public Plugins getNewPlugins(MainStore mainStore,Store store,PluginsService pluginsService,int count){
//        Plugins plugins1 = new Plugins();
//        plugins1.setId(0l);
//        plugins1.setType(pluginsService.getPluginsType());
//        plugins1.setName(pluginsService.getName());
//        plugins1.setMainStore(mainStore);
//        plugins1.setStore(store);
//        plugins1.setPluginsClassPath(pluginsService.getPluginsTag());
//        plugins1.setUuid(pluginsService.getPluginsUUID());
//        plugins1.setPicPath(pluginsService.getPicPath());
//
//
//
//        plugins1.setStartTime(new Date());
//        plugins1.setPicPath(pluginsService.getPicPath());
//        plugins1.setType(pluginsService.getPluginsType());
//
//        setPluginPrices(mainStore,store,plugins1,pluginsService);
//
//        int day  = pluginsService.getExpiryDay() * count;
//        if(day == 0){//永久
//            day = 365*100;//100年吧
//        }else if(pluginsService.getFreeExpiryDay() > 0){//有试用天数
//            day  = pluginsService.getFreeExpiryDay();
//        }
//        Calendar calendar2 = Calendar.getInstance();
//        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        calendar2.add(Calendar.DATE, day);
//        String endDate = sdf2.format(calendar2.getTime());
//        try {
//            plugins1.setEndTime(sdf2.parse(endDate));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//
//        if(plugins1.getPrice() == 0 || pluginsService.getFreeExpiryDay() > 0){//免费的或有试用时间的可以直接保存
//            plugins1 = pluginsRepository.saveAndFlush(plugins1);
//        }
//        return plugins1;
//    }
    /**
     * 给插件设置价格
     * @param mainStore
     * @param store
     * @param plugins
     * @param pluginsService
     */
    public void setPluginPrices(MainStore mainStore,Store store,Plugins plugins,PluginsService pluginsService){
        plugins.setVip0Price(pluginsService.getVip0Price());
        plugins.setVip1Price(pluginsService.getVip1Price());
        plugins.setVip2Price(pluginsService.getVip2Price());
        plugins.setDay(pluginsService.getExpiryDay());
        int vip = 0;
        if(store == null) {
            vip = mainStore.getVip();
        }else{
            vip = store.getMainStore().getVip();
        }
        if(vip == 0){
            plugins.setPrice(pluginsService.getVip0Price());
        }else if(vip == 1){
            plugins.setPrice(pluginsService.getVip1Price());
        }else if(vip == 2){
            plugins.setPrice(pluginsService.getVip2Price());
        }
    }
}

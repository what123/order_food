package com.food.order.controller.store;


import com.alibaba.fastjson.JSON;
import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.config.Config;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.OauthConfigRepository;
import com.food.order.model.service.PluginsServiceImpl;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.PluginsTypeEnum;
import com.food.order.plugins.oauth.OauthService;
import com.food.order.plugins.oauth.OauthUtil;
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
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "分店授权配置管理", description = "分店授权配置的接口",tags="分店-授权管理接口")
@RestController("store_oauth_config_controller")
@RequestMapping("/api/store/oauth_config")
@CrossOrigin
public class OauthConfigController extends BaseController {
    @Autowired
    OauthConfigRepository oauthConfigRepository;
    @Autowired
    PluginsServiceImpl pluginsServiceImpl;

    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "id",defaultValue = "0")Long id,
            @RequestParam(value = "store_id",defaultValue = "0")Long store_id,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");
        if (store.getConfigType() == 1){
            msgVo.setCode(40002);
            msgVo.setMsg("统一使用总部的配置");
            return msgVo;
        }

        List<Plugins> plugins = pluginsServiceImpl.getStoreAllPlugins(store, PluginsTypeEnum.OAUTH_PLUGINS.getIndex());
        Map<String,Plugins> pluginsMap = new HashMap<>();
        for (Plugins plugins1:plugins) {
            pluginsMap.put(plugins1.getUuid(),plugins1);
        }

        Criteria<OauthConfig> criteria = new Criteria<>();
        if(id > 0){
            criteria.add(Restrictions.eq("id",id));
        }
        criteria.add(Restrictions.eq("store",store));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        List<OauthConfig> oauthConfigs = oauthConfigRepository.findAll(criteria,sort);

        List<OauthService> oauthClazz = new OauthUtil().getOauthClass(null);
        //初始化数据

        Map<String,OauthConfig> tags = new HashMap<>();
        if (oauthConfigs != null && oauthConfigs.size() > 0) {
            for (OauthConfig oauthConfig : oauthConfigs) {
                tags.put(oauthConfig.getTag(),oauthConfig);
            }

        }
        for (OauthService oauthService : oauthClazz) {
            PluginsService pluginsService = (PluginsService) oauthService;
            if (!tags.containsKey(pluginsService.getPluginsTag())) {
                OauthConfig oauthConfig = new OauthConfig();
                oauthConfig.setStore(store);
                oauthConfig.setTag(pluginsService.getPluginsTag());
                oauthConfig.setName(pluginsService.getName());
                Map<String, String> map = ((PluginsService) oauthService).getParamsConfig();
                Map<String, Map<String, String>> map2 = new HashMap<>();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    Map<String, String> values = new HashMap<>();
                    values.put("placeholder", entry.getValue());
                    values.put("value", "");
                    map2.put(entry.getKey(), values);
                }
                oauthConfig.setParamsStr(JSON.toJSONString(map2));
                oauthConfig = oauthConfigRepository.saveAndFlush(oauthConfig);
                if(pluginsMap.containsKey(pluginsService.getPluginsUUID())){
                    Plugins plugins1 = pluginsMap.get(pluginsService.getPluginsUUID());
                    pluginsServiceImpl.setPluginPrices(null,store,plugins1,pluginsService);
                    oauthConfig.setPlugins(plugins1);
                }else {
                    Plugins plugins1 = pluginsServiceImpl.getNewPlugins(null,store,pluginsService);
                    oauthConfig.setPlugins(plugins1);
                }
                oauthConfigs.add(oauthConfig);
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


        msgVo.getData().put("oauth_callback_url", Config.host + "/api/consumer/oauth_callback/");
        msgVo.getData().put("oauthConfigs",oauthConfigs);
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

        OauthConfig oauthConfig = null;
        if(id > 0){
            oauthConfig = oauthConfigRepository.findById(id).orElse(null);
            if(oauthConfig == null || oauthConfig.getStore().getUser().getId() != store.getUser().getId()){
                msgVo.setCode(40002);
                msgVo.setMsg("该配置不存在");
                return msgVo;
            }
        }else{
            msgVo.setCode(40003);
            msgVo.setMsg("该配置不存在");
            return msgVo;
        }
        oauthConfig.setStatus(status);
        oauthConfig.setParamsStr(params);
        oauthConfigRepository.save(oauthConfig);

        return msgVo;
    }

    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    @ResponseBody
    public MsgVo delete(@PathVariable("id")Long id){
        MsgVo msgVo = new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");
        OauthConfig oauthConfig = oauthConfigRepository.findById(id).orElse(null);
        if(oauthConfig == null || oauthConfig.getMainStore().getUser().getId() != mainStore.getUser().getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("配置不存在");
            return msgVo;
        }
        oauthConfigRepository.delete(oauthConfig);
        return msgVo;
    }
}

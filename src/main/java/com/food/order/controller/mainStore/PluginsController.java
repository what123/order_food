package com.food.order.controller.mainStore;

import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.MainStore;
import com.food.order.model.entity.Plugins;
import com.food.order.model.repository.*;
import com.food.order.model.service.PluginsServiceImpl;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.PluginsTypeEnum;
import com.food.order.plugins.PluginsUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api( value = "插件管理", description = "插件接口",tags="店家-插件接口")
@Controller("main_store_plugins")
@RequestMapping("/api/main_store/plugins")
@CrossOrigin
public class PluginsController extends BaseController{

    @Autowired
    PluginsServiceImpl pluginsServiceImpl;
    @Autowired
    PluginsRepository pluginsRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MainStoreRepository mainStoreRepository;
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    PaymentsConfigRepository paymentsConfigRepository;



    @ApiOperation(value="列表-获取插件列表", notes="列表-获取插件列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(@RequestParam(value = "keyword",defaultValue = "")String keyword,
                      @RequestParam(value = "plugins_price_id",defaultValue = "0")Long plugins_price_id,
                      @RequestParam(value = "page",defaultValue = "1")Integer page,
                      @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                      HttpServletRequest request){
        MsgVo msgVo =new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");

        List<Plugins> pluginsList = pluginsServiceImpl.getMainStoreAllPlugins(mainStore, 0);
        Map<String,Plugins> pluginsMap = new HashMap<>();
        for (Plugins plugins1:pluginsList) {
            pluginsMap.put(plugins1.getUuid(),plugins1);
        }

        Map<Integer,List<Plugins>> allPluginsMap = new HashMap<>();
        //int vip = mainStore.getVip();
        PluginsUtils pluginsUtils = new PluginsUtils();
        List<PluginsService> pluginServices = pluginsUtils.getPluginsClass();
        if(pluginsList.size() == 0){
            for(PluginsService pluginsService:pluginServices){
                Plugins plugins = pluginsServiceImpl.getNewPlugins(mainStore,null,pluginsService);
                if(allPluginsMap.containsKey(plugins.getType())){
                    allPluginsMap.get(plugins.getType()).add(plugins);
                }else{
                    List<Plugins> plugins1 = new ArrayList<>();
                    plugins1.add(plugins);
                    allPluginsMap.put(plugins.getType(),plugins1);
                }

            }
        }else{
            for(PluginsService pluginsService:pluginServices){
                Plugins plugins = null;
                if(pluginsMap.containsKey(pluginsService.getPluginsUUID())){
                    plugins = pluginsMap.get(pluginsService.getPluginsUUID());
                    plugins.setDay(pluginsService.getExpiryDay());
                    plugins.setNote(pluginsService.getNote());
                    pluginsServiceImpl.setPluginPrices(mainStore,null,plugins,pluginsService);
                }else{
                    plugins = pluginsServiceImpl.getNewPlugins(mainStore,null,pluginsService);
                }


                if(allPluginsMap.containsKey(plugins.getType())){
                    allPluginsMap.get(plugins.getType()).add(plugins);
                }else{
                    List<Plugins> plugins1 = new ArrayList<>();
                    plugins1.add(plugins);
                    allPluginsMap.put(plugins.getType(),plugins1);
                }
            }

        }

        //TODO 树形结构
        List<Plugins> pluginsType = new ArrayList<>();
        for(PluginsTypeEnum pluginsTypeEnum:PluginsTypeEnum.values()){
            Plugins plugins = new Plugins();
            plugins.setType(pluginsTypeEnum.getIndex());
            plugins.setName(pluginsTypeEnum.getName());
            pluginsType.add(plugins);
        }
        msgVo.getData().put("pluginsTypes",pluginsType);
        msgVo.getData().put("plugins",allPluginsMap);
        msgVo.setMsg("获取成功");
        return msgVo;
    }





}

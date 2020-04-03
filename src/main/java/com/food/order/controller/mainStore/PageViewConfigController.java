package com.food.order.controller.mainStore;


import com.alibaba.fastjson.JSON;
import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.entity.*;
import com.food.order.model.repository.MainStoreRepository;
import com.food.order.model.repository.PageViewRepository;
import com.food.order.model.repository.PluginsRepository;
import com.food.order.model.repository.StoreRepository;
import com.food.order.model.service.PluginsServiceImpl;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.PluginsTypeEnum;
import com.food.order.plugins.PluginsUtils;
import com.food.order.plugins.pageView.PageViewService;
import com.food.order.plugins.pageView.PageViewTarget;
import com.food.order.plugins.pageView.PageViewUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.applet.Main;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店家点餐页面配置管理", description = "店家点餐页面配置的接口",tags="店家-点餐页面管理接口")
@RestController("main_store_consumer_view_config_controller")
@RequestMapping("/api/main_store/consumer_view_config")
@CrossOrigin
public class PageViewConfigController extends BaseController {
    @Autowired
    MainStoreRepository mainStoreRepository;
    @Autowired
    PluginsServiceImpl pluginsServiceImpl;

    @Autowired
    PluginsRepository pluginsRepository;
    @Autowired
    PageViewRepository pageViewRepository;

    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(HttpServletRequest request){
        MsgVo msgVo =new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");

        List<Plugins> pageViews = pluginsServiceImpl.getMainStoreAllPlugins(mainStore, PluginsTypeEnum.PAGE_VIEW_PLUGINS.getIndex());
        //TODO 增加一些推荐的
        Map<String,Plugins> pluginsMap = new HashMap<>();

        for(Plugins plugins:pageViews){
            pluginsMap.put(plugins.getPluginsClassPath(),plugins);
        }


        List<Plugins> plugins = new ArrayList<>();
        Long pageViewId = mainStore.getPageViewId();
        List<PluginsService> pageViewServices = new PluginsUtils().getPluginsClass(PageViewTarget.class);
        for(PluginsService pageViewsService:pageViewServices){
            Plugins plugins1 = null;
            if(pluginsMap.containsKey(pageViewsService.getPluginsTag())){
                plugins1 = pluginsMap.get(pageViewsService.getPluginsTag());
                pluginsServiceImpl.setPluginPrices(mainStore,null,plugins1,pageViewsService);
            }else{
                plugins1 = pluginsServiceImpl.getNewPlugins(mainStore,null,pageViewsService);
            }
            plugins.add(plugins1);
            if(pageViewId == null || pageViewId == 0) {
                PageViewService pageViewService = (PageViewService) pageViewsService;
                if (pageViewService.getViewPath().equals("default")) {
                    pageViewId = plugins1.getId();
                    mainStore.setPageViewId(pageViewId);
                    mainStoreRepository.save(mainStore);
                }
            }
        }

        msgVo.getData().put("consumerViews",plugins);
        msgVo.getData().put("usedPageViewId",pageViewId);
        msgVo.setMsg("获取成功");
        return msgVo;
    }


    @ApiOperation(value="选择启用主题", notes="选择启用主题")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo add(@PathVariable(value = "id")Long id){
        MsgVo msgVo = new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");
        if(id > 0){
            Plugins plugins = pluginsRepository.findById(id).orElse(null);
            if(plugins == null || plugins.getType() != PluginsTypeEnum.PAGE_VIEW_PLUGINS.getIndex()){//不存在
                msgVo.setCode(40002);
                msgVo.setMsg("该主题不存在");
                return msgVo;
            }
            if(plugins.getMainStore().getId() != mainStore.getId()){//也不是自己所属店家买的，则不可以以使用
                msgVo.setCode(40002);
                msgVo.setMsg("请先购买该主题");
                return msgVo;
            }
            MsgVo msgVo1 = pluginsServiceImpl.checkPlugins(mainStore,null,plugins.getPluginsClassPath());
            if(msgVo1.getCode() != 0){
                return msgVo1;
            }
            mainStore.setPageViewId(id);
            mainStoreRepository.save(mainStore);
        }else{
            msgVo.setCode(40003);
            msgVo.setMsg("必须传正确的id");
            return msgVo;
        }
        return msgVo;
    }
}

package com.food.order.controller.store;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
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
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "分店点餐页面配置管理", description = "分店点餐页面配置的接口",tags="分店-点餐页面管理接口")
@RestController("store_consumer_view_config_controller")
@RequestMapping("/api/store/consumer_view_config")
@CrossOrigin
public class PageViewConfigController extends BaseController {
    @Autowired
    StoreRepository storeRepository;
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
            HttpServletRequest request) throws ClassNotFoundException {

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");
        if (store.getConfigType() == 1){
            msgVo.setCode(40002);
            msgVo.setMsg("统一使用总部的配置");
            return msgVo;
        }

        List<Plugins> pageViews = pluginsServiceImpl.getStoreAllPlugins(store, PluginsTypeEnum.PAGE_VIEW_PLUGINS.getIndex());
        //TODO 增加一些推荐的
        Map<String,Plugins> pluginsMap = new HashMap<>();

        for(Plugins plugins:pageViews){
            pluginsMap.put(plugins.getPluginsClassPath(),plugins);
        }


        List<Plugins> plugins = new ArrayList<>();
        Long pageViewId = store.getPageViewId();
        List<PluginsService> pageViewServices = new PluginsUtils().getPluginsClass(PageViewTarget.class);
        for(PluginsService pageViewsService:pageViewServices){
            Plugins plugins1 = null;
            if(pluginsMap.containsKey(pageViewsService.getPluginsTag())){
                plugins1 = pluginsMap.get(pageViewsService.getPluginsTag());
                pluginsServiceImpl.setPluginPrices(null,store,plugins1,pageViewsService);
            }else{
                plugins1 = pluginsServiceImpl.getNewPlugins(null,store,pageViewsService);
            }
            plugins.add(plugins1);
            if(pageViewId == null || pageViewId == 0) {
                PageViewService pageViewService = (PageViewService) pageViewsService;
                if (pageViewService.getViewPath().equals("default")) {
                    pageViewId = plugins1.getId();
                    store.setPageViewId(pageViewId);
                    storeRepository.save(store);
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
        Store store = (Store) request.getAttribute("user");
        if (store.getConfigType() == 1){
            msgVo.setCode(40002);
            msgVo.setMsg("统一使用总部的配置");
            return msgVo;
        }
        if(id > 0){
            Plugins plugins = pluginsRepository.findById(id).orElse(null);
            if(plugins == null || plugins.getType() != PluginsTypeEnum.PAGE_VIEW_PLUGINS.getIndex()){//不存在
                msgVo.setCode(40002);
                msgVo.setMsg("该主题不存在");
                return msgVo;
            }
            if(plugins.getStore() == null || plugins.getStore().getId() != store.getId()){//不是自己的，则查是否为上级店家购买的
                if(plugins.getMainStore().getId() != store.getMainStore().getId()){//也不是自己所属店家买的，则不可以以使用
                    msgVo.setCode(40002);
                    msgVo.setMsg("请先购买该主题");
                    return msgVo;
                }
            }
            MsgVo msgVo1 = pluginsServiceImpl.checkPlugins(null,store,plugins.getPluginsClassPath());
            if(msgVo1.getCode() != 0){
                return msgVo1;
            }
            store.setPageViewId(id);
            storeRepository.save(store);
        }else{
            msgVo.setCode(40003);
            msgVo.setMsg("请先购买该主题");
            return msgVo;
        }
        return msgVo;
    }
}

package com.food.order.controller.consumer;

import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.entity.Consumer;
import com.food.order.model.entity.Plugins;
import com.food.order.model.service.PluginsServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Api( value = "插件", description = "插件接口",tags="消费者-插件接口")
@Controller("consumer_plugins")
@RequestMapping("/api/consumer/plugins")
@CrossOrigin
public class PluginsController extends BaseController{

    @Autowired
    PluginsServiceImpl pluginsServiceImpl;



    @ApiOperation(value="列表-获取插件列表", notes="列表-获取插件列表")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(HttpServletRequest request){
        MsgVo msgVo =new MsgVo();
        Consumer consumer = (Consumer) request.getAttribute("user");
        List<Plugins> pluginsList = pluginsServiceImpl.getStoreAllPlugins(consumer.getStore(), 0);

        List<String> uuids = new ArrayList<>();//客户端要显示的插件

        List<Plugins> pluginsList1 = new ArrayList<>();
        for(Plugins plugins:pluginsList){
            if(plugins.getStatus() == 4 && uuids.contains(plugins.getPluginsClassPath()) && plugins.isUsed()) {
                pluginsList1.add(plugins);
            }
        }

        msgVo.getData().put("plugins",pluginsList1);
        msgVo.setMsg("获取成功");
        return msgVo;
    }
}

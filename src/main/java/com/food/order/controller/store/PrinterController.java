package com.food.order.controller.store;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.model.service.PluginsServiceImpl;
import com.food.order.plugins.PluginsData;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.PluginsTypeEnum;
import com.food.order.plugins.printer.PrinterDataModeTypeEnum;
import com.food.order.plugins.printer.PrinterService;
import com.food.order.plugins.printer.PrinterUtil;
import com.food.order.plugins.printer.feie.BaseFeie;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
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
@Api( value = "分店打印机管理", description = "分店打印机的接口",tags="分店-打印机接口")
@RestController("store_printer_controller")
@RequestMapping("/api/store/printer")
@CrossOrigin
public class PrinterController extends BaseController {
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    PrinterRepository printerRepository;
    @Autowired
    PrinterDataModelRepository printerDataModelRepository;
    @Autowired
    PluginsRepository pluginsRepository;
    @Autowired
    PluginsServiceImpl pluginsServiceImpl;

    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(@RequestParam(value = "id",defaultValue = "0")Long id,@RequestParam(value = "order_and_maked",defaultValue = "0")int order_and_maked, HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Criteria<Printer> criteria = new Criteria<>();
        if(id > 0){
            criteria.add(Restrictions.eq("id",id));
        }


        criteria.add(Restrictions.eq("store",store));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        List<Printer> printers = printerRepository.findAll(criteria,sort);

        if(order_and_maked > 0){//获取出餐类型
            List<Printer> printers1 = new ArrayList<>();
            for(Printer printer:printers){
                List<PrinterDataModel> printerDataModels = printer.getPrinterDataModels();
                if(printerDataModels != null && printerDataModels.size() > 0){
                    for(PrinterDataModel printerDataModel:printerDataModels){
                        if(!printers1.contains(printer)) {
                            if (printerDataModel.getType() == PrinterDataModeTypeEnum.ORDER_GOOD.getIndex() || printerDataModel.getType() == PrinterDataModeTypeEnum.MAKED.getIndex()) {
                                printers1.add(printer);
                            }
                        }
                    }
                }
            }
            printers = printers1;

        }



        msgVo.getData().put("printers",printers);

        msgVo.setMsg("获取成功");
        return msgVo;
    }

    @ApiOperation(value="打印机品牌列表", notes="打印机品牌列表")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "/brand_and_model",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo brand(HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");


        Criteria<PrinterDataModel> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",store));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        List<PrinterDataModel> printerDataModels = printerDataModelRepository.findAll(criteria,sort);
        if(printerDataModels == null && printerDataModels.size() == 0){
            msgVo.setCode(40001);
            msgVo.setMsg("请先配置好打印数据模板");
            return msgVo;
        }
        msgVo.getData().put("printers_data_model",printerDataModels);

        List<Plugins> pageViews = pluginsServiceImpl.getStoreAllPlugins(store, PluginsTypeEnum.PAGE_VIEW_PLUGINS.getIndex());
        Map<String,Plugins> pluginsMap = new HashMap<>();
        for(Plugins plugins:pageViews){

            pluginsMap.put(plugins.getPluginsClassPath(),plugins);
        }

        List<PrinterService> printerServices = new PrinterUtil().getPrinterClass(null);
        List<Printer>  printers = new ArrayList<>();
        for(PrinterService printerService:printerServices){
            PluginsService pluginsService = (PluginsService)printerService;

            if(!pluginsMap.containsKey(pluginsService.getPluginsType())){//未购买的看是不是免费的
                Plugins plugins = pluginsServiceImpl.getNewPlugins(null,store,pluginsService);

                if(plugins.getStatus() != 4) {//未购买的不能使用
                    continue;
                }
            }
            Printer printer = new Printer();
            PluginsService pluginsService1 = (PluginsService)printerService;
            printer.setName(pluginsService1.getName());
            printer.setApiTag(pluginsService1.getPluginsTag());
            printer.setPluginUUID(pluginsService1.getPluginsUUID());

            Map<String, String> map = ((PluginsService) printerService).getParamsConfig();
            Map<String, Map<String, String>> map2 = new HashMap<>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                Map<String, String> values = new HashMap<>();
                values.put("placeholder", entry.getValue());
                values.put("value", "");
                map2.put(entry.getKey(), values);
            }
            printer.setApiConfigParams(JSON.toJSONString(map2));
            printers.add(printer);
        }
        msgVo.getData().put("printers_brand",printers);



        msgVo.setMsg("获取成功");
        return msgVo;
    }

    @ApiOperation(value="创建/修改", notes="创建/修改")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo add(@RequestParam(value = "id",defaultValue = "0")Long id,
                     @RequestParam("name")String name,
                     @RequestParam("pluginUUID")String uuid,
                     @RequestParam("status")int status,
                     @RequestParam("params")String apiConfigParms,//接口配置
                     @RequestParam("data_model_ids")String data_ids//打印数据的模板id

    ){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");

        List<PrinterService> printerServices = new PrinterUtil().getPrinterClassByUUID(uuid);

        if(printerServices.size() == 0){
            msgVo.setCode(40002);
            msgVo.setMsg("打印机品牌不存在");
            return msgVo;
        }
        PluginsService pluginsService = (PluginsService)printerServices.get(0);
        Printer printer = null;
        if(id > 0){
            printer = printerRepository.findById(id).orElse(null);
            if(printer == null || printer.getStore().getId() != store.getId()){
                msgVo.setCode(40001);
                msgVo.setMsg("打印机不存在");
                return msgVo;
            }
        }else{
            printer = new Printer();
            printer.setStore(store);
        }
        printer.setStatus(status);
        printer.setName(name);
        printer.setApiTag(pluginsService.getPluginsTag());
        printer.setPluginUUID(pluginsService.getPluginsUUID());
        //通过打印机品牌获得配置数据
        printer.setApiConfigParams(apiConfigParms);

        if(StringUtils.isNotEmpty(data_ids.trim())){
            String[] dataIds = data_ids.split("#");
            List<String> dataIdsList = Arrays.asList(dataIds);
            Criteria<PrinterDataModel> criteria = new Criteria<>();
            criteria.add(Restrictions.in("id", dataIdsList));
            List<PrinterDataModel> printerDataModels = printerDataModelRepository.findAll(criteria);
            printer.setPrinterDataModels(printerDataModels);
        }else{
            printer.getPrinterDataModels().clear();
        }



        PluginsData printerData = null;
        if(id > 0){
            printerData = printerServices.get(0).updatePrinter(printer);
        }else{
            printerData = printerServices.get(0).addPrinter(printer);
        }

        if(printerData.getCode() == 200) {
            printerRepository.save(printer);
        }else{
            msgVo.setCode(4003);
            msgVo.setMsg("云端添加打印机失败:"+printerData.getMsg());
        }
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
        Printer printer = printerRepository.findById(id).orElse(null);
        if(printer == null || printer.getStore().getId() != store.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("打印机不存在");
            return msgVo;
        }
        List<PrinterService> printerServices = new PrinterUtil().getPrinterClass(printer.getApiTag());
        if(printerServices.size() == 0){
            msgVo.setCode(40002);
            msgVo.setMsg("打印机品牌不存在");
            return msgVo;
        }

        PluginsData printerData = printerServices.get(0).delPrinter(printer);

        if(printerData.getCode() == 200) {
            printerRepository.delete(printer);
        }else{
            msgVo.setCode(4003);
            msgVo.setMsg(printerData.getMsg());
        }

        return msgVo;
    }

    @ApiOperation(value="打印", notes="打印")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}/order/{order_id}/type/{type}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo printData(@PathVariable("id")Long id,@PathVariable("order_id")Long order_id,@PathVariable("type")int type){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        msgVo = printOrderById(store,id,order_id,type);
        return msgVo;
    }

}

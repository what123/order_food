package com.food.order.controller.storeUser;


import com.alibaba.fastjson.JSON;
import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.PluginsRepository;
import com.food.order.model.repository.PrinterDataModelRepository;
import com.food.order.model.repository.PrinterRepository;
import com.food.order.model.repository.StoreRepository;
import com.food.order.model.service.PluginsServiceImpl;
import com.food.order.plugins.PluginsData;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.PluginsTypeEnum;
import com.food.order.plugins.printer.PrinterDataModeTypeEnum;
import com.food.order.plugins.printer.PrinterService;
import com.food.order.plugins.printer.PrinterUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店员打印机管理", description = "店员打印机的接口",tags="店员-打印机接口")
@RestController("store_user_printer_controller")
@RequestMapping("/api/store_user/printer")
@CrossOrigin
public class PrinterController extends BaseController {
    @Autowired
    PrinterRepository printerRepository;

    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(@RequestParam(value = "id",defaultValue = "0")Long id,@RequestParam(value = "order_and_maked",defaultValue = "0")int order_and_maked, HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        Criteria<Printer> criteria = new Criteria<>();
        if(id > 0){
            criteria.add(Restrictions.eq("id",id));
        }


        criteria.add(Restrictions.eq("store",storeUser.getStore()));
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


    @ApiOperation(value="打印", notes="打印")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}/order/{order_id}/type/{type}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo printData(@PathVariable("id")Long id,@PathVariable("order_id")Long order_id,@PathVariable("type")int type){
        MsgVo msgVo = new MsgVo();

        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        msgVo = printOrderById(storeUser.getStore(),id,order_id,type);
        return msgVo;
    }

}

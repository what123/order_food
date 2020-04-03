package com.food.order.controller.mainStore;


import com.alibaba.fastjson.JSON;
import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.PrinterDataModelRepository;
import com.food.order.model.repository.PrinterRepository;
import com.food.order.model.repository.StoreRepository;
import com.food.order.plugins.printer.PrinterDataModeTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店家打印数据模型", description = "店家打印数据的接口",tags="店家-打印数据模型接口")
@RestController("main_store_printer_data_model_controller")
@RequestMapping("/api/main_store/printer_data_model")
@CrossOrigin
public class PrinterDataModelController extends BaseController {
    @Autowired
    PrinterDataModelRepository printerDataModelRepository;



    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "id",defaultValue = "0")Long id,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");
        Criteria<PrinterDataModel> criteria = new Criteria<>();
        if(id > 0){
            criteria.add(Restrictions.eq("id",id));
        }
        criteria.add(Restrictions.eq("mainStore",mainStore));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        List<PrinterDataModel> printerDataModels = printerDataModelRepository.findAll(criteria,sort);
        PrinterDataModeTypeEnum[] printerTypeEnums = PrinterDataModeTypeEnum.values();
        //初始化数据
        int size = printerTypeEnums.length;
        if(size > printerDataModels.size()) {
            List<Integer> types = new ArrayList<>();
            if (printerDataModels != null && printerDataModels.size() > 0) {
                for (PrinterDataModel printerDataModel : printerDataModels) {
                    types.add(printerDataModel.getType());
                }

            }
            for (PrinterDataModeTypeEnum printerTypeEnum : printerTypeEnums) {
                if (!types.contains(printerTypeEnum.getIndex())) {
                    PrinterDataModel printerDataModel = new PrinterDataModel();
                    printerDataModel.setMainStore(mainStore);
                    printerDataModel.setType(printerTypeEnum.getIndex());
                    printerDataModel.setName(PrinterDataModeTypeEnum.getName(printerTypeEnum.getIndex()) + "模板");
                    printerDataModelRepository.saveAndFlush(printerDataModel);
                }
            }
            printerDataModels = printerDataModelRepository.findAll(criteria, sort);
        }
        msgVo.getData().put("printerDataModels",printerDataModels);
        msgVo.setMsg("获取成功");
        return msgVo;
    }


    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo add(@PathVariable(value = "id")Long id,
                     @RequestParam("printData")String data//模板数据

    ){
        MsgVo msgVo = new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");

        PrinterDataModel printerDataModel = null;
        if(id > 0){
            printerDataModel = printerDataModelRepository.findById(id).orElse(null);
            if(printerDataModel == null || printerDataModel.getMainStore().getId() != mainStore.getId()){
                msgVo.setCode(40002);
                msgVo.setMsg("模板不存在");
                return msgVo;
            }
        }else{
            msgVo.setCode(40003);
            msgVo.setMsg("模板不存在");
            return msgVo;
        }
        //通过打印机品牌获得配置数据
        printerDataModel.setPrintData(data);
        printerDataModelRepository.save(printerDataModel);
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
        PrinterDataModel printerDataModel = printerDataModelRepository.findById(id).orElse(null);
        if(printerDataModel == null || printerDataModel.getMainStore().getId() != mainStore.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("模板不存在");
            return msgVo;
        }
        printerDataModelRepository.delete(printerDataModel);
        return msgVo;
    }

}

package com.food.order.controller.mainStore;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.ChargeItem;
import com.food.order.model.entity.MainStore;
import com.food.order.model.repository.ChargeItemRepository;
import com.food.order.model.service.PluginsServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店家充值选项", description = "店家充值选项的接口",tags="店家-充值选项")
@RestController("main_store_charge_item_controller")
@RequestMapping("/api/main_store/charge_item")
@CrossOrigin
public class ChargeItemsController extends BaseController {
    @Autowired
    ChargeItemRepository chargeItemRepository;
    @Autowired
    PluginsServiceImpl pluginsServiceImpl;

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
        String plugin_msg = null;

        Criteria<ChargeItem> criteria = new Criteria<>();
        if(id > 0){
            criteria.add(Restrictions.eq("id",id));
        }
        criteria.add(Restrictions.eq("mainStore",mainStore));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<ChargeItem> chargeItems = chargeItemRepository.findAll(criteria,pageable);
        msgVo.getData().put("charge_items",chargeItems);
        msgVo.getData().put("plugin_msg",plugin_msg);
        msgVo.setMsg("获取成功");
        return msgVo;
    }


    @ApiOperation(value="创建/修改", notes="创建/修改")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo add(@RequestParam(value = "id",defaultValue = "0")Long id,
                     @RequestParam("chargePrice")Integer chargePrice,
                     @RequestParam("totalPrice")Integer totalPrice,
                     @RequestParam("status")Integer status,
                     @RequestParam(value = "tag",defaultValue = "")String tag
    ){
        MsgVo msgVo = new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");

        ChargeItem chargeItem = null;
        if(id > 0){
            chargeItem = chargeItemRepository.findById(id).orElse(null);
            if(chargeItem == null || chargeItem.getMainStore().getId() != mainStore.getId()){
                msgVo.setCode(40002);
                msgVo.setMsg("充值项不存在");
                return msgVo;
            }
        }else{
            chargeItem = new ChargeItem();
            chargeItem.setMainStore(mainStore);
        }

        chargeItem.setChargePrice(chargePrice);
        chargeItem.setTotalPrice(totalPrice);
        chargeItem.setTag(tag);
        chargeItem.setStatus(status);
        chargeItemRepository.save(chargeItem);
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

        ChargeItem chargeItem = chargeItemRepository.findById(id).orElse(null);
        if(chargeItem == null || chargeItem.getMainStore().getId() != mainStore.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("充值项不存在");
            return msgVo;
        }
        chargeItemRepository.delete(chargeItem);
        return msgVo;
    }

}

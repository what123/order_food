package com.food.order.controller.storeUser;


import com.alibaba.fastjson.JSON;
import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.plugins.printer.PrinterDataModeTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "分店员菜品制作", description = "分店员菜品制作接口",tags="店员-菜品制作接口")
@RestController("store_user_order_goods_controller")
@RequestMapping("/api/store_user/order_goods")
@CrossOrigin
public class OrderGoodsController extends BaseController {
    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    OrdersRepository ordersRepository;
    @Autowired
    OrderGoodsRepository orderGoodsRepository;


    @ApiOperation(value="菜品队列", notes="菜品队列")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/queue",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo goodsQueue(){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        Criteria<OrderGoods> goodsCriteria = new Criteria<>();
        goodsCriteria.add(Restrictions.eq("store",storeUser.getStore()));
        List<Long> statusList = new ArrayList<>();
        statusList.add(1l);
        statusList.add(2l);
        goodsCriteria.add(Restrictions.in("status",statusList));
        goodsCriteria.add(Restrictions.eq("payStatus",1));
        goodsCriteria.add(Restrictions.eq("isDelete",false));
        Sort sort = new Sort(Sort.Direction.ASC, "id");
        List<OrderGoods> orderGoods = orderGoodsRepository.findAll(goodsCriteria,sort);

        List<OrderGoods> allOrderGoods = new ArrayList<>();
        if(orderGoods == null || orderGoods.size() == 0){
            msgVo.getData().put("queue",allOrderGoods);
            msgVo.setMsg("暂无菜品队列");
            return msgVo;
        }
        for(OrderGoods orderGoods1:orderGoods){
            if(orderGoods1.getOrders().getApplyStatus() == 2){//必须是确单的
                allOrderGoods.add(orderGoods1);
            }
        }
        if(allOrderGoods == null || allOrderGoods.size() == 0){
            msgVo.getData().put("queue",allOrderGoods);
            msgVo.setMsg("暂无菜品队列");
            return msgVo;
        }
        Collections.sort(allOrderGoods, new Comparator<OrderGoods>() {
            @Override
            public int compare(OrderGoods o1, OrderGoods o2) {
                int diff = o1.getStatus() - o2.getStatus();
                if (diff < 0) {
                    return 1;
                } else if (diff > 0) {
                    return -1;
                }
                return 0; //相等为0
            }
        });
        msgVo.getData().put("queue",allOrderGoods);
        //printOrderById(id,storeUser.getStore().getId(), PrinterDataModeTypeEnum.ORDER.getIndex());//改单打印
        return msgVo;
    }

    @ApiOperation(value="开始制作菜品", notes="开始制作菜品")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}/making",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo makingOrderGoods(@PathVariable("id")Long id){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        OrderGoods orderGoods = orderGoodsRepository.findById(id).orElse(null);
        if(orderGoods == null || orderGoods.getOrders().getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("菜品单不存在");
            return msgVo;
        }
        orderGoods.setMakingCount(orderGoods.getCount());
        orderGoods.setStatus(1);
        orderGoodsRepository.save(orderGoods);
        return msgVo;
    }

    @ApiOperation(value="完成菜品制作", notes="完成菜品制作")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}/maked",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo makedOrderGoods(@PathVariable("id")Long id){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        OrderGoods orderGoods = orderGoodsRepository.findById(id).orElse(null);
        if(orderGoods == null || orderGoods.getOrders().getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("菜品单不存在");
            return msgVo;
        }
        orderGoods.setMakedCount(orderGoods.getMakingCount());
        orderGoods.setStatus(1);
        orderGoodsRepository.save(orderGoods);
        printOrderGoodsById(orderGoods.getId(),PrinterDataModeTypeEnum.MAKED.getIndex(),false);
        return msgVo;
    }

}

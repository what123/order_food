package com.food.order.controller.mainStore;


import com.alibaba.excel.support.ExcelTypeEnum;
import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.utils.utils.excel.utils.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店家订单管理", description = "店家订单的接口",tags="店家-订单接口")
@RestController("main_store_orders_controller")
@RequestMapping("/api/main_store/orders")
@CrossOrigin
public class OrdersController extends BaseController {
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    OrdersRepository ordersRepository;



    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "order_id",defaultValue = "0")Long order_id,
            @RequestParam(value = "store_id",defaultValue = "0")Long store_id,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            @RequestParam(value = "keyword",defaultValue = "")String keyword,
            @RequestParam(value = "created_start_time",defaultValue = "")String created_start_time,
            @RequestParam(value = "created_end_time",defaultValue = "")String created_end_time,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");
        //查找对应的分店
        Criteria<Orders> criteria = new Criteria<>();
        if(store_id > 0){
            Store store = storeRepository.findById(store_id).orElse(null);
            if(store == null || store.getMainStore().getId() != mainStore.getId()){
                msgVo.setCode(40001);
                msgVo.setMsg("分店不存在");
                return msgVo;
            }
            criteria.add(Restrictions.eq("store",store));
        }else {
            Criteria<Store> storeCriteria = new Criteria<>();
            storeCriteria.add(Restrictions.eq("mainStore", mainStore));
            List<Store> stores = storeRepository.findAll(storeCriteria);
            criteria.add(Restrictions.in("store",stores));
        }
        if(order_id > 0){
            criteria.add(Restrictions.eq("id",order_id));
        }

        criteria.add(Restrictions.gte("payStatus",2));

        List<Integer> types = new ArrayList<>();
        types.add(OrderTypeEnum.GOODS_ORDERS.getIndex());
        types.add(OrderTypeEnum.CHARGE_ORDERS.getIndex());
        //types.add(OrderTypeEnum.PLUGINS_ORDERS.getIndex());
        criteria.add(Restrictions.in("type",types));
        if(!keyword.trim().equals("")){
            criteria.add(Restrictions.or(Restrictions.like("orderNo","%"+keyword+"%"),Restrictions.like("tableNumber","%"+keyword+"%")));
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if(StringUtils.isEmpty(created_start_time) && StringUtils.isNotEmpty(created_end_time)){
                criteria.add(Restrictions.lte("createTime",simpleDateFormat.parse(created_end_time)));
            }else if(StringUtils.isNotEmpty(created_start_time) && StringUtils.isEmpty(created_end_time)){
                criteria.add(Restrictions.gte("createTime",simpleDateFormat.parse(created_start_time)));
            }else if(StringUtils.isNotEmpty(created_start_time) && StringUtils.isNotEmpty(created_end_time)){
                criteria.add(Restrictions.between("createTime",simpleDateFormat.parse(created_start_time),simpleDateFormat.parse(created_end_time)));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<Orders> orders = ordersRepository.findAll(criteria,pageable);
        msgVo.getData().put("orders",orders);
        msgVo.setMsg("获取成功");
        return msgVo;
    }
    @ApiOperation(value="列表-获取指定订单的菜品", notes="列表-获取指定订单的菜品")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/orderGoods",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo goods(
            @RequestParam(value = "order_id")Long order_id,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");

        Orders orders = ordersRepository.findById(order_id).orElse(null);
        if(orders == null || orders.getStore().getMainStore().getId() != mainStore.getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }

        Criteria<OrderGoods> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("orders",orders));
        List<OrderGoods> orderGoods = orderGoodsRepository.findAll(criteria);

        msgVo.getData().put("orderGoods",orderGoods);
        msgVo.setMsg("获取成功");
        return msgVo;
    }
    @ApiOperation(value="excle导出订单", notes="excle导出订单")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "/excel", method = {RequestMethod.GET})
    public void downloadEmailsExcel(HttpServletResponse response,
                                    @RequestParam(value = "store_id")Long store_id,
                                    @RequestParam(value = "keyword",defaultValue = "")String keyword,
                                    @RequestParam(value = "created_start_time",defaultValue = "")String created_start_time,
                                    @RequestParam(value = "created_end_time",defaultValue = "")String created_end_time) throws UnsupportedEncodingException {

        MainStore mainStore = (MainStore) request.getAttribute("user");

        //查找对应的分店
        Criteria<Orders> criteria = new Criteria<>();
        if(store_id > 0){
            Store store = storeRepository.findById(store_id).orElse(null);
            if(store == null || store.getMainStore().getId() != mainStore.getId()){
               return;
            }
            criteria.add(Restrictions.eq("store",store));
        }else {
            Criteria<Store> storeCriteria = new Criteria<>();
            storeCriteria.add(Restrictions.eq("mainStore", mainStore));
            List<Store> stores = storeRepository.findAll(storeCriteria);
            criteria.add(Restrictions.in("store",stores));
        }
        criteria.add(Restrictions.gte("payStatus",2));


        List<Integer> types = new ArrayList<>();
        types.add(OrderTypeEnum.GOODS_ORDERS.getIndex());
        types.add(OrderTypeEnum.CHARGE_ORDERS.getIndex());
        //types.add(OrderTypeEnum.PLUGINS_ORDERS.getIndex());
        criteria.add(Restrictions.in("type",types));
        if(!keyword.trim().equals("")){
            criteria.add(Restrictions.or(Restrictions.like("orderNo","%"+keyword+"%"),Restrictions.like("tableNumber","%"+keyword+"%")));
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(StringUtils.isEmpty(created_start_time)){
            return;
        }
        if(StringUtils.isEmpty(created_end_time)){
            return;
        }
        try {
            criteria.add(Restrictions.between("createTime",simpleDateFormat.parse(created_start_time),simpleDateFormat.parse(created_end_time)));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        List<Orders> orders = ordersRepository.findAll(criteria);
        List<OrderExcel> orderExcels = new ArrayList<>();
        for (Orders order:orders){
            OrderExcel orderExcel = new OrderExcel();
            orderExcel.setOrder_no(order.getOrderNo());
            orderExcel.setCreated_time(simpleDateFormat.format(order.getCreateTime()));
            if(order.getPayTime() != null) {
                orderExcel.setPay_time(simpleDateFormat.format(order.getPayTime()));
            }else{
                orderExcel.setPay_time("未支付");
            }
            orderExcel.setPayType(order.getPayTypeStr());

            orderExcel.setTotal_price(""+order.getTotalPrice()/100);
            orderExcel.setReal_price(""+order.getRealPrice()/100);
            orderExcel.setRefund_price(""+order.getRefundPrice()/100);
            orderExcel.setStoreName(order.getStore().getName());
            orderExcels.add(orderExcel);
        }
        try {
            ExcelUtil.writeExcel(response,orderExcels, "orders", "Sheet1", ExcelTypeEnum.XLSX, OrderExcel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

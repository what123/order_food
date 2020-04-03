package com.food.order.controller.store;


import com.alibaba.excel.support.ExcelTypeEnum;
import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.config.Config;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.model.service.OrdersServiceImpl;
import com.food.order.plugins.PluginsData;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.pay.PayUtil;
import com.food.order.plugins.pay.PaymentService;
import com.food.order.plugins.pay.balance.Balance;
import com.food.order.plugins.printer.PrinterDataModeTypeEnum;
import com.food.order.utils.utils.excel.utils.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javafx.scene.control.Tab;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "分店订单管理", description = "分店订单的接口",tags="分店-订单接口")
@RestController("store_orders_controller")
@RequestMapping("/api/store/orders")
@CrossOrigin
public class OrdersController extends BaseController {
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    OrdersRepository ordersRepository;
    @Autowired
    OrderGoodsRepository orderGoodsRepository;
    @Autowired
    TablesRepository tablesRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    PaymentsConfigRepository paymentsConfigRepository;
    @Autowired
    OrdersServiceImpl ordersServiceImpl;

    @Autowired
    UserSettingRepository userSettingRepository;

    @Autowired
    VipUserCardRepository vipUserCardRepository;
    @Autowired
    ConsumerRepository consumerRepository;


    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "order_id",defaultValue = "0")Long order_id,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            @RequestParam(value = "keyword",defaultValue = "")String keyword,
            @RequestParam(value = "payStatus",defaultValue = "0")Integer payStatus,
            @RequestParam(value = "created_start_time",defaultValue = "")String created_start_time,
            @RequestParam(value = "created_end_time",defaultValue = "")String created_end_time,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Criteria<Orders> criteria = new Criteria<>();
        if(order_id > 0){
            criteria.add(Restrictions.eq("id",order_id));
        }
        if(payStatus > 0){
            criteria.add(Restrictions.eq("payStatus",payStatus));
        }
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
        List<Integer> types = new ArrayList<>();
        types.add(OrderTypeEnum.GOODS_ORDERS.getIndex());
        types.add(OrderTypeEnum.CHARGE_ORDERS.getIndex());
        //types.add(OrderTypeEnum.PLUGINS_ORDERS.getIndex());
        criteria.add(Restrictions.in("type",types));
        criteria.add(Restrictions.eq("store",store));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<Orders> orders = ordersRepository.findAll(criteria,pageable);
        msgVo.getData().put("orders",orders);
        msgVo.setMsg("获取成功");
        return msgVo;
    }




    @ApiOperation(value="修改订单中的实收价格", notes="修改订单中的实收价格")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo updatePrice(@PathVariable("id")Long id,@RequestParam(value = "real_price")Integer real_price){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Orders orders = ordersRepository.findById(id).orElse(null);
        if(orders == null || orders.getStore().getId() != store.getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }
        saveStoreLog(store,null,"分店管理员修改订单"+orders.getOrderNo()+"的实收价格（原实收价:"+orders.getRealPrice()/100+"元）为"+(real_price/100)+"元");
        //重新计算并更新订单价格
        //updateOrderPrice(orders,0);
        orders.setRealPrice(real_price);
        ordersRepository.save(orders);
        return msgVo;
    }

    @ApiOperation(value="手动支付", notes="手动支付")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}/pay",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo pay(@PathVariable("id")Long id,@RequestParam(value = "print",defaultValue = "1")Integer print){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Orders orders = ordersRepository.findById(id).orElse(null);
        if(orders == null || orders.getStore().getId() != store.getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }
        if(orders.getPayStatus() != 1){
            msgVo.setCode(40003);
            msgVo.setMsg("订单已经被支付过了");
            return msgVo;
        }
        saveStoreLog(store,null,"分店管理员手动支付订单"+orders.getOrderNo()+",实收价:"+orders.getRealPrice()/100+"元");
        orders.setPayTypeStr("手动支付");
        orders.setPayStatus(2);
        orders.setApplyStatus(2);
        orders.setPayUser(store.getUser());
        orders.setPayTime(new Date());
        ordersRepository.save(orders);

        //更新菜品支付状态
        Criteria<OrderGoods> orderGoodsCriteria = new Criteria<>();
        orderGoodsCriteria.add(Restrictions.eq("orders", orders));
        List<OrderGoods> orderGoodsList = orderGoodsRepository.findAll(orderGoodsCriteria);
        for(OrderGoods orderGoods1:orderGoodsList) {
            orderGoods1.setPayStatus(1);
            orderGoodsRepository.save(orderGoods1);
        }

        //还原该桌子的状态
        Criteria<Tables> criteria1 = new Criteria<>();
        criteria1.add(Restrictions.eq("number", orders.getTableNumber()));
        criteria1.add(Restrictions.eq("store", store));
        criteria1.add(Restrictions.eq("isDelete", false));
        Tables tables1 = tablesRepository.findOne(criteria1).orElse(null);
        tables1.setStatus(1);
        tablesRepository.save(tables1);

        if(print == 1){
            // 打印订单

            printOrderById(orders.getId(),PrinterDataModeTypeEnum.PAY_ORDER.getIndex(),false);

        }

        Criteria<UserConfigs> userConfigsCriteria = new Criteria<>();
        userConfigsCriteria.add(Restrictions.eq("store",orders.getStore()));
        userConfigsCriteria.add(Restrictions.eq("valueKey",UserConfigTypeEnum.ORDERS_GOODS_TYPE.getIndex()));
        UserConfigs userConfigs = userSettingRepository.findOne(userConfigsCriteria).orElse(null);
        String orderGoodsType = "1";
        if(userConfigs != null){
            orderGoodsType = userConfigs.getValueStr();
        }

        if(orders.getStore().getPayConfig() == 1 && orderGoodsType.equals("2")){//预付费且打印模式
            printOrderGoodsByOrderId(orders.getId(),PrinterDataModeTypeEnum.ORDER_GOOD.getIndex(),false);//向厨房打印订单
            for(OrderGoods orderGoods1:orderGoodsList) {
                orderGoods1.setMakingCount(orderGoods1.getCount());
                orderGoods1.setMakedCount(orderGoods1.getCount());
                orderGoodsRepository.save(orderGoods1);
            }
        }

        return msgVo;
    }

    @ApiOperation(value="菜品退款", notes="菜品退款")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/order_goods/{goods_id}/refund",method = RequestMethod.PUT)
    @ResponseBody
    @Transactional
    public MsgVo refund(@PathVariable("goods_id")Long goods_id,@RequestParam(value = "note",defaultValue = "")String note){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        OrderGoods orderGoods = orderGoodsRepository.findById(goods_id).orElse(null);
        if(orderGoods == null || orderGoods.getStore().getId() != store.getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("订单菜品不存在");
            return msgVo;
        }
        //TODO 事务处理
        // 退款
        Orders orders = orderGoods.getOrders();

        Long payment_id = orders.getPaymentId();
        if(payment_id != null && payment_id > 0) {
            PaymentsConfig paymentsConfig = paymentsConfigRepository.findById(payment_id).orElse(null);
            List<PaymentService> paymentServices = new PayUtil().getPaymentClass(paymentsConfig.getTag());
            if (paymentServices.size() == 0) {
                msgVo.setCode(40002);
                msgVo.setMsg("退款插件不存在，可能是支付配置不正常");
                return msgVo;
            }

            if (orders.getPayStatus() == 1) {
                msgVo.setCode(40003);
                msgVo.setMsg("未支付订单不可退款");
                return msgVo;
            }
            if (orders.getPayStatus() == 4) {
                msgVo.setCode(40004);
                msgVo.setMsg("该订单已全部退款");
                return msgVo;
            }
            PaymentService paymentService = paymentServices.get(0);
            paymentService.setParams(paymentsConfig, Config.host);
            if (orders.getRefundPrice() == null) {
                orders.setRefundPrice(orderGoods.getRealPrice());
            } else {
                orders.setRefundPrice(orders.getRefundPrice() + orderGoods.getRealPrice());//退款金额
            }
            String payTag = paymentsConfig.getTag();

            List<PaymentService> paymentServices1 = new PayUtil().getPayjsClass(payTag);//payjs只能全部退款
            if (paymentServices1.size() > 0) {
                orders.setRefundPrice(orders.getRealPrice());
            }

            PluginsData payData = paymentService.getRefundSendData(orders.getOrderNo(), orders.getOutOrderNo(), orders.getOrderNo() + orderGoods.getId(), orders.getRealPrice(), orderGoods.getRealPrice());
            if (payData.getCode() == 100) {
                msgVo.setCode(40003);
                msgVo.setMsg("退款接口异常，请检测对应的支付参数配置:" + payData.getMsg());
                return msgVo;
            }
            PluginsData payData2 = paymentService.refund(payData);
            if (payData2.getCode() == 100) {
                msgVo.setCode(40004);
                msgVo.setMsg("退款接口异常，请检测对应的支付参数配置:" + payData2.getMsg());
                return msgVo;
            }
        }else{//手动支付
            orderGoods.setRefundPrice(orderGoods.getRefundPrice());
            if (orders.getRefundPrice() == null) {
                orders.setRefundPrice(orderGoods.getRealPrice());
            } else {
                orders.setRefundPrice(orders.getRefundPrice() + orderGoods.getRealPrice());//退款金额
            }
        }


        if(orders.getRealPrice() <= orders.getRefundPrice()){//全额退了，主要是payjs,要把所有菜品都设置为退款状态
            Criteria<OrderGoods> orderGoodsCriteria = new Criteria<>();
            orderGoodsCriteria.add(Restrictions.eq("orders",orders));
            orderGoodsCriteria.add(Restrictions.eq("payStatus",1));
            List<OrderGoods> orderGoods1 = orderGoodsRepository.findAll(orderGoodsCriteria);
            if(orderGoods1.size() > 0) {
                for (OrderGoods orderGoods2 : orderGoods1) {
                    orderGoods2.setRefundPrice(orderGoods2.getRealPrice());
                    orderGoods2.setPayStatus(2);
                }
                orderGoodsRepository.saveAll(orderGoods1);
            }
            orders.setPayStatus(4);
        }else{
            orders.setPayStatus(3);

        }
        orderGoods.setPayStatus(2);
        orderGoodsRepository.save(orderGoods);

        if(StringUtils.isNotEmpty(note)){
            if(orders.getRefundNote() != null) {
                orders.setRefundNote(orders.getRefundNote() + "\n" + note);
            }else{
                orders.setRefundNote(note);
            }
        }
        ordersRepository.save(orders);

        saveStoreLog(store,null,"分店管理员手动退款订单"+orders.getOrderNo()+"退款:"+orders.getRefundPrice()/100+"元");

        updateOrderPrice(orderGoods.getOrders(),0);


        return msgVo;
    }
    @ApiOperation(value="订单退款", notes="订单退款")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{order_id}/refund",method = RequestMethod.PUT)
    @ResponseBody
    @Transactional
    public MsgVo orderRefund(@PathVariable("order_id")Long order_id,@RequestParam(value = "note",defaultValue = "")String note){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Orders orders = ordersRepository.findById(order_id).orElse(null);
        if(orders == null || orders.getStore().getId() != store.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }
        //TODO 事务处理
        // 退款
        Long payment_id = orders.getPaymentId();
        if(payment_id != null && payment_id > 0) {
            PaymentsConfig paymentsConfig = paymentsConfigRepository.findById(payment_id).orElse(null);
            if (paymentsConfig == null) {
                msgVo.setCode(40002);
                msgVo.setMsg("该订单不支持原路退款");
                return msgVo;
            }

            String payTag = paymentsConfig.getTag();
            List<PaymentService> paymentServices = new PayUtil().getPaymentClass(payTag);
            if (paymentServices.size() == 0) {
                msgVo.setCode(40002);
                msgVo.setMsg("退款插件不存在，可能是支付配置不正常");
                return msgVo;
            }

            if (orders.getPayStatus() == 1) {
                msgVo.setCode(40003);
                msgVo.setMsg("未支付订单不可退款");
                return msgVo;
            }
            if (orders.getPayStatus() == 4) {
                msgVo.setCode(40004);
                msgVo.setMsg("该订单已全部退款");
                return msgVo;
            }

            PaymentService paymentService = paymentServices.get(0);
            paymentService.setParams(paymentsConfig, Config.host);
            Integer refundFee = 0;
            if (orders.getRefundPrice() == null) {
                refundFee = orders.getRealPrice();
            } else {
                refundFee = orders.getRealPrice() - orders.getRefundPrice();
            }

//            List<PaymentService> paymentServices1 = new PayUtil().getPayjsClass(payTag);//payjs只能全部退款
//            if (paymentServices1.size() > 0) {
                orders.setRefundPrice(orders.getRealPrice());//订单退款肯定是全额退
//            }

            PluginsData payData = paymentService.getRefundSendData(orders.getOrderNo(), orders.getOutOrderNo(), orders.getOrderNo() + 0, orders.getRealPrice(), refundFee);
            if (payData.getCode() == 100) {
                msgVo.setCode(40003);
                msgVo.setMsg("退款接口异常，请检测对应的支付参数配置:" + payData.getMsg());
                return msgVo;
            }
            PluginsData payData2 = paymentService.refund(payData);
            if (payData2.getCode() == 100) {
                msgVo.setCode(40004);
                msgVo.setMsg("退款接口异常，请检测对应的支付参数配置:" + payData2.getMsg());
                return msgVo;
            }
            PluginsService pluginsService = (PluginsService) paymentService;
            if (pluginsService.getPluginsTag().equals(Balance.class.getName())) {//是余额支付的要退到余额

                // 查找是不是有对应的消费者
                Criteria<Consumer> consumerCriteria = new Criteria<>();
                consumerCriteria.add(Restrictions.eq("user",orders.getPayUser()));
                Consumer consumer = consumerRepository.findOne(consumerCriteria).orElse(null);

                if(consumer != null && !consumer.isDelete()) {
                    Criteria<VipUserCard> vipUserCardCriteria = new Criteria<>();
                    vipUserCardCriteria.add(Restrictions.eq("consumer", consumer));

                    VipUserCard vipUserCard = vipUserCardRepository.findOne(vipUserCardCriteria).orElse(null);
                    if (vipUserCard != null) {
                        if (vipUserCard.getBalance() == null) {
                            vipUserCard.setBalance(0);
                        }
                        vipUserCard.setBalance(vipUserCard.getBalance() + orders.getRealPrice());
                        vipUserCardRepository.saveAndFlush(vipUserCard);
                    }
                }
//                orders.getPayUser().setBalance(orders.getPayUser().getBelong() + orders.getRealPrice());
//                userRepository.save(orders.getPayUser());
            }
        }else{
            orders.setRefundPrice(orders.getRealPrice());//订单退款肯定是全额退
        }
        if(orders.getRealPrice() <= orders.getRefundPrice()){//全额退了，主要是payjs,要把所有菜品都设置为退款状态
            Criteria<OrderGoods> orderGoodsCriteria = new Criteria<>();
            orderGoodsCriteria.add(Restrictions.eq("orders",orders));
            orderGoodsCriteria.add(Restrictions.eq("payStatus",1));
            List<OrderGoods> orderGoods1 = orderGoodsRepository.findAll(orderGoodsCriteria);
            if(orderGoods1.size() > 0) {
                for (OrderGoods orderGoods2 : orderGoods1) {
                    orderGoods2.setRefundPrice(orderGoods2.getRealPrice());
                    orderGoods2.setPayStatus(2);
                }
                orderGoodsRepository.saveAll(orderGoods1);
            }
            orders.setPayStatus(4);
        }else{
            orders.setPayStatus(3);

        }
        if(StringUtils.isNotEmpty(note)){
            if(orders.getRefundNote() != null) {
                orders.setRefundNote(orders.getRefundNote() + "\n" + note);
            }else{
                orders.setRefundNote(note);
            }
        }
        ordersRepository.save(orders);
        saveStoreLog(store,null,"分店管理员手动退款订单"+orders.getOrderNo()+"退款:"+orders.getRefundPrice()/100+"元");
        return msgVo;
    }

    @ApiOperation(value="手动打印订单", notes="手动打印订单")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}/print/{printer_id}/type/{type}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo printOrder(@PathVariable("id")Long id,@PathVariable("printer_id")Integer printer_id,@PathVariable("type")Integer type){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Orders orders = ordersRepository.findById(id).orElse(null);
        if(orders == null || orders.getStore().getId() != store.getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }
        saveStoreLog(store,null,"分店管理员手动打印订单"+orders.getOrderNo());
        printOrderById(orders.getId(),type,false);
        return msgVo;
    }

    @ApiOperation(value="excle导出订单", notes="excle导出订单")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "/excel", method = {RequestMethod.GET})
    public void downloadEmailsExcel(HttpServletResponse response,
                                    @RequestParam(value = "keyword",defaultValue = "")String keyword,
                                    @RequestParam(value = "status",defaultValue = "0")Integer status,
                                    @RequestParam(value = "created_start_time",defaultValue = "")String created_start_time,
                                    @RequestParam(value = "created_end_time",defaultValue = "")String created_end_time) throws UnsupportedEncodingException {
        Store store = (Store) request.getAttribute("user");
        //查找对应的分店
        Criteria<Orders> criteria = new Criteria<>();

        List<Integer> types = new ArrayList<>();
        types.add(OrderTypeEnum.GOODS_ORDERS.getIndex());
        types.add(OrderTypeEnum.CHARGE_ORDERS.getIndex());
       // types.add(OrderTypeEnum.PLUGINS_ORDERS.getIndex());
        criteria.add(Restrictions.in("type",types));
       // criteria.add(Restrictions.gte("type",OrderTypeEnum.GOODS_ORDERS.getIndex()));
        criteria.add(Restrictions.eq("store",store));
        if(status > 0){
            criteria.add(Restrictions.eq("payStatus",status));
        }
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
            orderExcel.setStoreName(order.getStore().getName());
            orderExcels.add(orderExcel);
        }
        try {
            ExcelUtil.writeExcel(response,orderExcels, "orders", "Sheet1", ExcelTypeEnum.XLSX, OrderExcel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @ApiOperation(value="将某个用户加入黑名单(针对恶意创建订单者)", notes="将某个用户加入黑名单(针对恶意创建订单者)")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/blacklist/{uid}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo blacklist(@PathVariable("uid")Long uid){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        User user = userRepository.findById(uid).orElse(null);
        if(user == null){
            msgVo.setCode(40002);
            msgVo.setMsg("用户不存在");
            return msgVo;
        }
        saveStoreLog(store,null,"分店管理员将帐号为"+user.getAccount()+"的用户加入黑名单");
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
        Orders orders = ordersRepository.findById(id).orElse(null);
        if(orders == null || orders.getStore().getId() != store.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }
        if(orders.getApplyStatus() == 2){//已经确认的订单需要是未支付及已退款的才能删除
            if(orders.getPayStatus() == 2){
                msgVo.setCode(40002);
                msgVo.setMsg("已支付订单不能删除");
                return msgVo;
            }
        }
        // 对应的点的菜品也要删除
        Criteria<OrderGoods> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("orders",orders));
        List<OrderGoods> orderGoods = orderGoodsRepository.findAll(criteria);
        if(orderGoods.size() > 0) {
            orderGoodsRepository.deleteAll(orderGoods);
        }
        ordersRepository.delete(orders);
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
        Store storeUser = (Store) request.getAttribute("user");

        Orders orders = ordersRepository.findById(order_id).orElse(null);
        if(orders == null || orders.getStore().getId() != storeUser.getId()){
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


    @ApiOperation(value="确认订单（针对后付费机制）", notes="确认订单（针对后付费机制）")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}/status",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo updateStatus(@PathVariable("id")Long id,@RequestParam(value = "applyStatus",defaultValue = "2")Integer applyStatus){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Orders orders = ordersRepository.findById(id).orElse(null);
        if(orders == null || orders.getStore().getId() != store.getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }
        if(orders.getPayStatus() != 1){
            msgVo.setCode(40003);
            msgVo.setMsg("订单已经被支付过了");
            return msgVo;
        }
        saveStoreLog(store,null,"分店管理员（"+store.getName()+"）手动确认订单"+orders.getOrderNo());

        orders.setApplyStatus(applyStatus);

        ordersRepository.save(orders);


        if(applyStatus == 2){//确认为正常就可以打印了
            //修改该桌子的状态
            Criteria<Tables> criteria = new Criteria<>();
            criteria.add(Restrictions.eq("number", orders.getTableNumber()));
            criteria.add(Restrictions.eq("store", store));
            criteria.add(Restrictions.eq("isDelete", false));
            Tables tables1 = tablesRepository.findOne(criteria).orElse(null);
            tables1.setStatus(2);
            tablesRepository.save(tables1);
            // 打印订单
            printOrderById(orders.getId(),PrinterDataModeTypeEnum.ORDER.getIndex(),false);

            Criteria<UserConfigs> userConfigsCriteria = new Criteria<>();
            userConfigsCriteria.add(Restrictions.eq("store",orders.getStore()));
            userConfigsCriteria.add(Restrictions.eq("valueKey",UserConfigTypeEnum.ORDERS_GOODS_TYPE.getIndex()));
            UserConfigs userConfigs = userSettingRepository.findOne(userConfigsCriteria).orElse(null);
            String orderGoodsType = "1";
            if(userConfigs != null){
                orderGoodsType = userConfigs.getValueStr();
            }

            if(orders.getStore().getPayConfig() == 1 && orderGoodsType.equals("2")){//打印模式
                printOrderGoodsByOrderId(orders.getId(),PrinterDataModeTypeEnum.ORDER_GOOD.getIndex(),false);//向厨房打印订单

                Criteria<OrderGoods> orderGoodsCriteria = new Criteria<>();
                orderGoodsCriteria.add(Restrictions.eq("orders", orders));
                List<OrderGoods> orderGoodsList = orderGoodsRepository.findAll(orderGoodsCriteria);
                for(OrderGoods orderGoods1:orderGoodsList) {
                    orderGoods1.setMakingCount(orderGoods1.getCount());
                    orderGoods1.setMakedCount(orderGoods1.getCount());
                    orderGoodsRepository.save(orderGoods1);
                }
            }
        }
        return msgVo;
    }
}

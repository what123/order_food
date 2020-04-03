package com.food.order.controller.consumer;


import com.alibaba.fastjson.JSON;
import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.plugins.printer.PrinterDataModeTypeEnum;
import com.food.order.websocket.WebSocketServer;
import com.food.order.websocket.WebsocketTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "消费者订单管理", description = "消费者订单的接口",tags = "消费者-订单接口")
@RestController("consumer_orders_controller")
@RequestMapping("/api/consumer/orders")
@CrossOrigin
public class OrdersController extends BaseController {
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    StoreLogRepository storeLogRepository;
    @Autowired
    OrdersRepository ordersRepository;
    @Autowired
    OrderGoodsRepository orderGoodsRepository;
    @Autowired
    TablesRepository tablesRepository;
    @Autowired
    BlacklistRepository blacklistRepository;
    @Autowired
    UserSettingRepository userSettingRepository;
    @Autowired
    CartRepository cartRepository;
    @Autowired
    ShareCartRepository shareCartRepository;


    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "order_id",defaultValue = "0")Long order_id,
            @RequestParam(value = "table_id",defaultValue = "0")Long table_id,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "20")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Consumer consumer = (Consumer) request.getAttribute("user");
        Criteria<Orders> criteria = new Criteria<>();
        if(order_id > 0){
            criteria.add(Restrictions.eq("id",order_id));
        }
        criteria.add(Restrictions.eq("createdUser",consumer.getUser()));
        if(consumer.getWxOpenId().equals("-1")){//临时的只能查看3小时内的最后一单
            pageSize = 1;
            if(table_id <= 0){//临时的必须指定桌号
                msgVo.setCode(40001);
                msgVo.setMsg("餐桌id不能为空");
                return msgVo;
            }
           // SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar beforeTime = Calendar.getInstance();
            beforeTime.add(Calendar.HOUR, -3);// 3小时之前的时间
            Date beforeD = beforeTime.getTime();
            criteria.add(Restrictions.or(Restrictions.gte("createTime",beforeD),Restrictions.eq("payStatus",1)));//未支付或者3小时前的订单
        }

        if(table_id > 0){
            Tables tables = tablesRepository.findById(table_id).orElse(null);
            if(tables == null && tables.getStore().getId() != consumer.getStore().getId()){
                msgVo.setCode(40002);
                msgVo.setMsg("餐桌不存在");
                return msgVo;
            }
            criteria.add(Restrictions.eq("tableNumber",tables.getNumber()));
        }
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<Orders> orders = ordersRepository.findAll(criteria,pageable);

        if(order_id > 0 && orders.getContent() != null && orders.getContent().size() > 0){//订单详情，获取菜品信息
            Orders orders1 = orders.getContent().get(0);
            Criteria<OrderGoods> goodsCriteria = new Criteria<>();
            goodsCriteria.add(Restrictions.eq("orders",orders1));
            goodsCriteria.add(Restrictions.eq("isDelete",false));
            List<OrderGoods> orderGoods = orderGoodsRepository.findAll(goodsCriteria);
            msgVo.getData().put("orderGoods",orderGoods);
            msgVo.getData().put("order",orders1);
        }else{
            msgVo.getData().put("orders",orders);
        }
        msgVo.setMsg("获取成功");
        return msgVo;
    }

    @ApiOperation(value="通过桌子的购物车创建订单", notes="通过桌子的购物车创建订单")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{table_id}",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo add(
                     @PathVariable("table_id")Long table_id,
                     @RequestParam(value = "note",defaultValue = "")String note,
                     @RequestParam(value = "people_num",defaultValue = "")Integer people_num){
        MsgVo msgVo = new MsgVo();
        Consumer consumer = (Consumer) request.getAttribute("user");

        Criteria<Blacklist> blacklistCriteria = new Criteria<>();
        blacklistCriteria.add(Restrictions.eq("store",consumer.getStore()));
        blacklistCriteria.add(Restrictions.eq("consumer",consumer));
        Blacklist blacklist = blacklistRepository.findOne(blacklistCriteria).orElse(null);
        if(blacklist != null) {
            msgVo.setCode(40002);
            msgVo.setMsg("您的帐号检测到有风险,已被临时禁止下单,如需下单，请联系店员");
            return msgVo;
        }
        User user = consumer.getUser();

        Tables table = tablesRepository.findById(table_id).orElse(null);
        if(table == null || table.getStore().getId() != consumer.getStore().getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("餐桌不存在");
            return msgVo;
        }

        Criteria<Cart> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",consumer.getStore()));
        if(consumer.getWxOpenId().equals("-1")) {//临时帐号下单要根据它的桌号
            criteria.add(Restrictions.eq("tables", table));
        }


        String share_pwd = null;

        String pwdKey = "share_cart_consumer_id"+table.getStore().getId()+"-"+consumer.getUser().getId();
        TmpCache tmpCache = tmpCacheRepository.findOneByCKey(pwdKey);
        if(tmpCache != null){
            share_pwd = tmpCache.getCValue();
        }

        ShareCart shareCart = null;
        if(StringUtils.isNotEmpty(share_pwd)) {//用密码来获取
            Criteria<ShareCart> shareCartCriteria = new Criteria<>();
            shareCartCriteria.add(Restrictions.eq("tables", table));
            shareCartCriteria.add(Restrictions.eq("store", table.getStore()));
            shareCartCriteria.add(Restrictions.eq("sharePassword", share_pwd));
            shareCart = shareCartRepository.findOne(shareCartCriteria).orElse(null);
            if(shareCart == null){
                msgVo.setCode(40002);
                msgVo.setMsg("没有对应的暗号点餐购物车");
                return msgVo;
            }
            criteria.add(Restrictions.eq("shareCart", shareCart));
        }else {
            criteria.add(Restrictions.eq("consumer", consumer));
        }
        List<Cart> carts = cartRepository.findAll(criteria);
        if(carts.size() == 0){
            msgVo.setCode(40003);
            msgVo.setMsg("请先选择菜品");
            return msgVo;
        }

        Orders order = new Orders();
        order.setPeopleNum(people_num);
        order.setTableNumber(table.getNumber());
        order.setApplyStatus(1);
        order.setStore(consumer.getStore());
        order.setNote(note);
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMddHHmmss");

        String outTradeNo = user.getId()+""+simpleDateFormat1.format(new Date())+((int)(10+Math.random()*(99-10+1)));//MD5Util.MD5Encode(goodsList.get(0).getId()+"-"+user_id+"-"+System.currentTimeMillis());
        order.setCreatedUser(consumer.getUser());
        order.setOrderNo(outTradeNo);

        //确认订单模式
        Criteria<UserConfigs> userConfigsCriteria = new Criteria<>();
        userConfigsCriteria.add(Restrictions.eq("store",consumer.getStore()));
        userConfigsCriteria.add(Restrictions.eq("valueKey",UserConfigTypeEnum.ORDERS_SURE_TYPE.getIndex()));
        UserConfigs userConfigs = userSettingRepository.findOne(userConfigsCriteria).orElse(null);
        if(userConfigs != null && userConfigs.getValueStr().equals("2")){//自动确认订单
            order.setApplyStatus(2);
        }

        ordersRepository.saveAndFlush(order);

        //将这个桌子设置为使用中
        table.setStatus(2);
        tablesRepository.save(table);


        if(carts != null && carts.size() > 0) {
            List<OrderGoods> orderGoods = new ArrayList<>();
            Store store = consumer.getStore();
            for (Cart cart : carts) {
                OrderGoods orderGoods1 = new OrderGoods();
                orderGoods1.setGoodsName(cart.getGoods().getName());
                orderGoods1.setGoodsStr(JSON.toJSONString(cart.getGoods()));
                orderGoods1.setOrders(order);
                orderGoods1.setStore(store);
                orderGoods1.setNote(cart.getNote());
                orderGoods1.setCount(cart.getNum());
                orderGoods1.setGoodsId(cart.getGoods().getId());
                orderGoods.add(orderGoods1);
            }
            orderGoodsRepository.saveAll(orderGoods);
        }
        //重新计算并更新订单价格
        updateOrderPrice(order,0);


        // 清除当前桌的购物车数据
        cartRepository.deleteAll(carts);
        if(shareCart != null){
            List<Consumer> consumers = shareCart.getConsumerList();
            if(consumers.size() > 0) {
                Long nowCid = consumer.getId();
                Long storeId = table.getStore().getId();
                for (Consumer consumer1 : consumers) {
                    String pwdKey2 = "share_cart_consumer_id"+storeId+"-"+consumer1.getUser().getId();
                    TmpCache tmpCache2 = tmpCacheRepository.findOneByCKey(pwdKey2);
                    if(tmpCache2 != null){
                        tmpCacheRepository.delete(tmpCache2);
                    }
                    if(consumer1.getId() == nowCid){//不需要推给自己
                        continue;
                    }
                    try {//向客人端推送通知
                        Map<String, Object> map = new HashMap<>();
                        map.put("action", "del_share_cart");
                        map.put("user", consumer1);
                        WebSocketServer.sendInfo("" + consumer1.getUser().getId(), WebsocketTypeEnum.SHARE_CART_SERVICE.getIndex(), map);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            shareCartRepository.delete(shareCart);

        }
        try {//向店家推送通知
            Map<String,Object> map = new HashMap<>();
            map.put("order_no",order.getOrderNo());
            map.put("table_number",order.getTableNumber());
            WebSocketServer.sendInfo(""+order.getStore().getUser().getId(),WebsocketTypeEnum.NEW_ORDER.getIndex(),map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(order.getApplyStatus() == 2 && consumer.getStore().getPayConfig() == 2) {//后付费模式立即打印订单
            printOrderById(order.getId(), PrinterDataModeTypeEnum.ORDER.getIndex(), false);
        }


        Criteria<UserConfigs> userConfigsCriteria2 = new Criteria<>();
        userConfigsCriteria2.add(Restrictions.eq("store",order.getStore()));
        userConfigsCriteria2.add(Restrictions.eq("valueKey",UserConfigTypeEnum.ORDERS_GOODS_TYPE.getIndex()));
        UserConfigs userConfigs2 = userSettingRepository.findOne(userConfigsCriteria2).orElse(null);
        String orderGoodsType = "1";
        if(userConfigs2 != null){
            orderGoodsType = userConfigs2.getValueStr();
        }
        if(order.getApplyStatus() == 2 && consumer.getStore().getPayConfig() == 2 && orderGoodsType.equals("2")){//打印模式
            printOrderGoodsByOrderId(order.getId(),PrinterDataModeTypeEnum.ORDER_GOOD.getIndex(),false);//向厨房打印订单
            Criteria<OrderGoods> orderGoodsCriteria = new Criteria<>();
            orderGoodsCriteria.add(Restrictions.eq("orders", order));
            List<OrderGoods> orderGoodsList = orderGoodsRepository.findAll(orderGoodsCriteria);
            for(OrderGoods orderGoods1:orderGoodsList) {
                orderGoods1.setMakingCount(orderGoods1.getCount());
                orderGoods1.setMakedCount(orderGoods1.getCount());
                orderGoodsRepository.save(orderGoods1);
            }
        }

        msgVo.getData().put("order",order);
        return msgVo;
    }



}

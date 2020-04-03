package com.food.order.controller.storeUser;


import com.alibaba.fastjson.JSON;
import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.model.service.OrdersServiceImpl;
import com.food.order.plugins.printer.PrinterDataModeTypeEnum;
import com.food.order.websocket.WebSocketServer;
import com.food.order.websocket.WebsocketTypeEnum;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "分店员订单管理", description = "分店员订单的接口",tags="店员-订单接口")
@RestController("store_user_orders_controller")
@RequestMapping("/api/store_user/orders")
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
    CartRepository cartRepository;

    @Autowired
    OrdersServiceImpl ordersServiceImpl;

    @Autowired
    UserSettingRepository userSettingRepository;

    @ApiOperation(value="查看餐桌最后一单", notes="查看餐桌最后一单")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "/table/{table_id}",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @PathVariable(value = "table_id")Long table_id,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");

        Tables tables = tablesRepository.findById(table_id).orElse(null);
        if(tables == null || tables.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("餐桌不存在");
            return msgVo;
        }

        Criteria<Orders> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",storeUser.getStore()));
        criteria.add(Restrictions.eq("tableNumber",tables.getNumber()));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(0, 1, sort);
        Page<Orders> ordersPage = ordersRepository.findAll(criteria,pageable);
        if(ordersPage.getContent() == null || ordersPage.getContent().size() == 0){
            msgVo.setCode(40001);
            msgVo.setMsg("该桌子没有订单");
            return msgVo;
        }
        Orders order = ordersPage.getContent().get(0);



        msgVo.getData().put("order",order);

        Criteria<OrderGoods> orderGoodsCriteria = new Criteria<>();
        orderGoodsCriteria.add(Restrictions.eq("orders",order));
        List<OrderGoods> orderGoods = orderGoodsRepository.findAll(orderGoodsCriteria);
        if(orderGoods.size() > 0){
            for (OrderGoods orderGoods1:orderGoods){
                orderGoods1.getGoodsStr();
            }
        }
        msgVo.getData().put("orderGoods",orderGoods);

        msgVo.setMsg("获取成功");
        return msgVo;
    }





    @ApiOperation(value="增减订单中（未支付订单）的菜品", notes="增减订单中（未支付订单）的菜品")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}/goods/{goods_id}",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo addGoods(@PathVariable("id")Long id,
                          @PathVariable("goods_id")Long goods_id,
                          @RequestParam(value = "num")int num,
                          @RequestParam(value = "note",defaultValue = "")String note){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        Orders order = ordersRepository.findById(id).orElse(null);
        if(order == null || order.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }
        if(order.getPayStatus() > 1){
            msgVo.setCode(40002);
            msgVo.setMsg("订单已支付，无法增减菜品，请重新下单");
            return msgVo;
        }

        Goods goods = goodsRepository.findById(goods_id).orElse(null);
        if(goods == null || goods.getStore().getId() != order.getStore().getId()){
            msgVo.setCode(40003);
            msgVo.setMsg("菜品不存在");
            return msgVo;
        }
        Criteria<OrderGoods> orderGoodsCriteria = new Criteria<>();
        orderGoodsCriteria.add(Restrictions.eq("orders",order));
        orderGoodsCriteria.add(Restrictions.eq("goodsId",goods_id));
        List<OrderGoods> orderGoodsList = orderGoodsRepository.findAll(orderGoodsCriteria);

        OrderGoods orderGoods = null;
        if(orderGoodsList.size() > 0) {
            orderGoods = orderGoodsList.get(0);
        }
        boolean isAdd = false;
        int oldNum = 0;
        if(num > 0) {
            if (orderGoods == null) {
                orderGoods = new OrderGoods();
                orderGoods.setGoodsName(goods.getName());
                orderGoods.setGoodsStr(JSON.toJSONString(goods));
                orderGoods.setNote(note);
                orderGoods.setCount(num);
                orderGoods.setGoodsId(goods_id);
                orderGoods.setOrders(order);
                isAdd = true;
            } else {
                if (num < orderGoods.getMakingCount()) {//减少时，必须判断菜是否已经做完了

                    Criteria<UserConfigs> userConfigsCriteria = new Criteria<>();
                    userConfigsCriteria.add(Restrictions.eq("store",storeUser.getStore()));
                    userConfigsCriteria.add(Restrictions.eq("valueKey",UserConfigTypeEnum.ORDERS_GOODS_TYPE.getIndex()));
                    UserConfigs userConfigs = userSettingRepository.findOne(userConfigsCriteria).orElse(null);
                    String orderGoodsType = "1";
                    if(userConfigs != null){
                        orderGoodsType = userConfigs.getValueStr();
                    }

                    if(orderGoodsType.equals("1")) {//电脑模式下
                        msgVo.setCode(40004);
                        msgVo.setMsg("菜品已制作，无法减少");
                        return msgVo;
                    }
                }else{//新增加
                    if(orderGoods.getStatus() > 1){//已经制作过的菜品数据不修改，继续增加新的菜品
                        orderGoods = new OrderGoods();
                        orderGoods.setGoodsName(goods.getName());
                        orderGoods.setGoodsStr(JSON.toJSONString(goods));
                        orderGoods.setNote(note);
                        orderGoods.setCount(num);
                        orderGoods.setGoodsId(goods_id);
                        orderGoods.setOrders(order);
                    }
                    orderGoods.setCount(num);
                    isAdd = true;
                }
                orderGoods.setNote(note);
            }
            orderGoodsRepository.save(orderGoods);

            //重新计算并更新订单价格
            updateOrderPrice(order,0);
            if(isAdd){

                Criteria<UserConfigs> userConfigsCriteria = new Criteria<>();
                userConfigsCriteria.add(Restrictions.eq("store",storeUser.getStore()));
                userConfigsCriteria.add(Restrictions.eq("valueKey",UserConfigTypeEnum.ORDERS_GOODS_TYPE.getIndex()));
                UserConfigs userConfigs = userSettingRepository.findOne(userConfigsCriteria).orElse(null);
                String orderGoodsType = "1";
                if(userConfigs != null){
                    orderGoodsType = userConfigs.getValueStr();
                }

                if(orderGoods.getOrders().getStore().getPayConfig() == 2 && orderGoodsType.equals("2")){//打印模式
                    printOrderGoodsById(orderGoods.getId(),PrinterDataModeTypeEnum.ORDER_GOOD.getIndex(),true);
                    orderGoods.setMakingCount(orderGoods.getCount());
                    orderGoods.setMakedCount(orderGoods.getCount());
                    orderGoodsRepository.save(orderGoods);
                }
            }

        }else{
            if (orderGoods == null) {
                msgVo.setCode(40005);
                msgVo.setMsg("订单未变更");
                return msgVo;
            }else{
                if (num < orderGoods.getMakingCount()) {//不能少于已制作中的菜品
                    Criteria<UserConfigs> userConfigsCriteria = new Criteria<>();
                    userConfigsCriteria.add(Restrictions.eq("store",storeUser.getStore()));
                    userConfigsCriteria.add(Restrictions.eq("valueKey",UserConfigTypeEnum.ORDERS_GOODS_TYPE.getIndex()));
                    UserConfigs userConfigs = userSettingRepository.findOne(userConfigsCriteria).orElse(null);
                    String orderGoodsType = "1";
                    if(userConfigs != null){
                        orderGoodsType = userConfigs.getValueStr();
                    }

                    if(orderGoodsType.equals("1")) {//电脑模式下
                        msgVo.setCode(40006);
                        msgVo.setMsg("菜品已制作，无法减少");
                        return msgVo;
                    }
                }
                orderGoodsRepository.delete(orderGoods);
            }

            //重新计算并更新订单价格
            Integer refundPrice = updateOrderPrice(order,0);
        }

        saveStoreLog(storeUser.getStore(),storeUser,"分店店员将订单"+order.getOrderNo()+"中菜品"+orderGoods.getGoodsName()+"的数量从"+oldNum+"修改为"+num);

        return msgVo;
    }

    @ApiOperation(value="通过桌子的购物车创建订单", notes="通过桌子的购物车创建订单")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{table_id}",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo add(@PathVariable("table_id")Long table_id,
                     @RequestParam(value = "note",defaultValue = "")String note,
                     @RequestParam(value = "people_num",defaultValue = "")Integer people_num){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        User user = storeUser.getUser();



        Tables table = tablesRepository.findById(table_id).orElse(null);
        if(table == null || table.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("餐桌不存在");
            return msgVo;
        }

        Criteria<Cart> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",storeUser.getStore()));
        criteria.add(Restrictions.eq("tables",table));
        List<Cart> carts = cartRepository.findAll(criteria);
        if(carts.size() == 0){
            msgVo.setCode(40003);
            msgVo.setMsg("请先选择菜品");
            return msgVo;
        }
        Orders order = new Orders();
        order.setStore(storeUser.getStore());

        order.setPeopleNum(people_num);
        order.setTableNumber(table.getNumber());
        order.setNote(note);
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMddHHmmss");

        String outTradeNo = user.getId()+""+simpleDateFormat1.format(new Date())+((int)(10+Math.random()*(99-10+1)));//MD5Util.MD5Encode(goodsList.get(0).getId()+"-"+user_id+"-"+System.currentTimeMillis());
        order.setCreatedUser(storeUser.getUser());
        order.setOrderNo(outTradeNo);
        order.setApplyStatus(2);
        ordersRepository.saveAndFlush(order);

        //将这个桌子设置为使用中
        table.setStatus(2);
        tablesRepository.save(table);

        List<OrderGoods> orderGoods = new ArrayList<>();
        if(carts != null && carts.size() > 0) {
            Store store = storeUser.getStore();
            for (Cart cart : carts) {
                OrderGoods orderGoods1 = new OrderGoods();
                orderGoods1.setGoodsName(cart.getGoods().getName());
                orderGoods1.setGoodsStr(JSON.toJSONString(cart.getGoods()));
                orderGoods1.setOrders(order);
                orderGoods1.setStore(store);
                orderGoods1.setNote(cart.getNote());
                orderGoods1.setCount(cart.getNum());
                orderGoods1.setGoodsId(cart.getGoods().getId());
                orderGoods1 = orderGoodsRepository.saveAndFlush(orderGoods1);
                orderGoods.add(orderGoods1);
            }
        }
        //重新计算并更新订单价格
        updateOrderPrice(order,0);

        //打印订单
        printOrderById(order.getId(),PrinterDataModeTypeEnum.ORDER.getIndex(),false);

        Criteria<UserConfigs> userConfigsCriteria = new Criteria<>();
        userConfigsCriteria.add(Restrictions.eq("store",storeUser.getStore()));
        userConfigsCriteria.add(Restrictions.eq("valueKey",UserConfigTypeEnum.ORDERS_GOODS_TYPE.getIndex()));
        UserConfigs userConfigs = userSettingRepository.findOne(userConfigsCriteria).orElse(null);
        String orderGoodsType = "1";
        if(userConfigs != null){
            orderGoodsType = userConfigs.getValueStr();
        }
        if(storeUser.getStore().getPayConfig() == 2 && orderGoodsType.equals("2")){//后付费制且打印模式
            printOrderGoodsByOrderId(order.getId(),PrinterDataModeTypeEnum.ORDER_GOOD.getIndex(),false);
            for(OrderGoods orderGoods1:orderGoods) {
                orderGoods1.setMakingCount(orderGoods1.getCount());
                orderGoods1.setMakedCount(orderGoods1.getCount());
                orderGoodsRepository.save(orderGoods1);
            }
        }


        // 清除当前桌的购物车数据
        cartRepository.deleteAll(carts);

        try {//向店家推送通知

            Map<String,Object> map = new HashMap<>();
            map.put("order_no",order.getOrderNo());
            map.put("table_number",order.getTableNumber());
            WebSocketServer.sendInfo(""+order.getStore().getUser().getId(), WebsocketTypeEnum.NEW_ORDER.getIndex(),map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msgVo;
    }


    @ApiOperation(value="修改订单中的桌子", notes="修改订单中的桌子")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}/table",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo updateTable(@PathVariable("id")Long id,
                             @RequestParam(value = "table_number")String table_number){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        Criteria<Tables> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("number", table_number));
        criteria.add(Restrictions.eq("store", storeUser.getStore()));
        criteria.add(Restrictions.eq("isDelete", false));

        Tables tables = tablesRepository.findOne(criteria).orElse(null);
        if(tables == null || tables.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("餐桌不存在");
            return msgVo;
        }


        Orders orders = ordersRepository.findById(id).orElse(null);
        if(orders == null || orders.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }

        //还原该桌子的状态
        Criteria<Tables> criteria2 = new Criteria<>();
        criteria2.add(Restrictions.eq("number", orders.getTableNumber()));
        criteria2.add(Restrictions.eq("store", storeUser.getStore()));
        criteria2.add(Restrictions.eq("isDelete", false));
        Tables tables1 = tablesRepository.findOne(criteria2).orElse(null);
        tables1.setStatus(1);
        tablesRepository.save(tables1);

        saveStoreLog(storeUser.getStore(),storeUser,"分店店员修改订单"+orders.getOrderNo()+"的桌号（原桌号:"+orders.getTableNumber()+"）为"+tables.getNumber());
        orders.setTableNumber(tables.getNumber());
        ordersRepository.save(orders);

        //将这个桌子设置为使用中
        tables.setStatus(2);
        tablesRepository.save(tables);
        msgVo.getData().put("table",tables);
        return msgVo;
    }


    @ApiOperation(value="确认订单（针对后付费机制）", notes="确认订单（针对后付费机制）")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}/status",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo updateStatus(@PathVariable("id")Long id,@RequestParam(value = "applyStatus",defaultValue = "2")Integer applyStatus){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        Orders orders = ordersRepository.findById(id).orElse(null);
        if(orders == null || orders.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }
        if(orders.getPayStatus() != 1){
            msgVo.setCode(40003);
            msgVo.setMsg("订单已经被支付过了");
            return msgVo;
        }
        saveStoreLog(storeUser.getStore(),storeUser,"分店店员（"+storeUser.getName()+"）手动确认订单"+orders.getOrderNo());

        orders.setApplyStatus(applyStatus);

        ordersRepository.save(orders);


        if(applyStatus == 2){//确认为正常就可以打印了
            //修改该桌子的状态
            Criteria<Tables> criteria = new Criteria<>();
            criteria.add(Restrictions.eq("number", orders.getTableNumber()));
            criteria.add(Restrictions.eq("store", storeUser.getStore()));
            criteria.add(Restrictions.eq("isDelete", false));
            Tables tables1 = tablesRepository.findOne(criteria).orElse(null);
            tables1.setStatus(2);
            tablesRepository.save(tables1);
            // 打印订单
            printOrderById(orders.getId(),PrinterDataModeTypeEnum.ORDER.getIndex(),false);
        }
        return msgVo;
    }



    @ApiOperation(value="列表-获取指定订单的菜品", notes="列表-获取指定订单的菜品")
    @ApiImplicitParams({
   })
    @RequestMapping(value = "/orderGoods",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo goods(
            @RequestParam(value = "order_id")Long order_id,
            @RequestParam(value = "keyword",defaultValue = "")String keyword,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");

        Orders orders = ordersRepository.findById(order_id).orElse(null);
        if(orders == null || orders.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }

        Criteria<OrderGoods> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("orders",orders));
        List<OrderGoods> orderGoods = orderGoodsRepository.findAll(criteria);
        Map<Long,OrderGoods> goodsNumMap = new HashMap<>();
        if(orderGoods.size() > 0){
            for(OrderGoods orderGoods1:orderGoods){
                goodsNumMap.put(orderGoods1.getGoods().getId(),orderGoods1);
            }
        }



        Criteria<Goods> goodsCriteria = new Criteria<>();
        goodsCriteria.add(Restrictions.eq("store",storeUser.getStore()));
        goodsCriteria.add(Restrictions.eq("status",2));
        goodsCriteria.add(Restrictions.eq("isDelete",false));
        if(!keyword.trim().equals("")){
            goodsCriteria.add(Restrictions.or(Restrictions.like("name","%"+keyword+"%"),Restrictions.like("number","%"+keyword+"%")));
        }

        List<Goods> goods = goodsRepository.findAll(goodsCriteria);

        List<OrderGoods> orderGoodsData = new ArrayList<>();
        if(goodsNumMap.size() > 0 && goods.size() > 0) {
            for (Goods goods1 : goods) {
                Long gid = goods1.getId();
                if (!goodsNumMap.containsKey(gid)) {
                    OrderGoods orderGoods1 = new OrderGoods();
                    orderGoods1.setOrders(orders);
                    orderGoods1.setGoodsId(gid);
                    orderGoods1.setGoodsStr(JSON.toJSONString(goods1));
                    orderGoods1.setGoodsName(goods1.getName());
                    orderGoods1.setCount(0);
                    orderGoods1.setNote("");
                    orderGoodsData.add(orderGoods1);
                } else {
                    orderGoodsData.add(goodsNumMap.get(gid));
                }

            }
            Collections.sort(orderGoodsData, new Comparator<OrderGoods>() {
                @Override
                public int compare(OrderGoods o1, OrderGoods o2) {
                    int diff = o1.getCount() - o2.getCount();
                    if (diff < 0) {
                        return 1;
                    } else if (diff > 0) {
                        return -1;
                    }
                    return 0; //相等为0
                }
            });
        }
        msgVo.getData().put("orderGoods",orderGoodsData);
        msgVo.setMsg("获取成功");
        return msgVo;
    }

    @ApiOperation(value="完成上菜", notes="完成上菜")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/order_goods/{order_goods_id}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo overOrderGoods(@PathVariable("order_goods_id")Long order_goods_id){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        OrderGoods orderGoods = orderGoodsRepository.findById(order_goods_id).orElse(null);
        if(orderGoods == null || orderGoods.getOrders().getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }
        orderGoods.setOverCount(orderGoods.getMakingCount());
        orderGoodsRepository.save(orderGoods);
        saveStoreLog(storeUser.getStore(),storeUser,"分店店员将订单"+orderGoods.getOrders().getOrderNo()+"中菜品"+orderGoods.getGoodsName()+"的完成上菜数量改为"+orderGoods.getMakingCount());
        return msgVo;
    }

    @ApiOperation(value="打印订单", notes="打印订单")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}/print",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo printOrder(@PathVariable("id")Long id){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        Orders order = ordersRepository.findById(id).orElse(null);
        if(order == null || order.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("订单不存在");
            return msgVo;
        }
        printOrderById(id,PrinterDataModeTypeEnum.ORDER.getIndex(),false);//改单打印
        return msgVo;
    }


}

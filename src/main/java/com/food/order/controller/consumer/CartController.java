package com.food.order.controller.consumer;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
import com.food.order.websocket.WebSocketServer;
import com.food.order.websocket.WebsocketTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "消费者购物车管理", description = "消费者购物车的接口",tags="消费者-购物车接口")
@RestController("consumer_cart_controller")
@RequestMapping("/api/consumer/cart")
@CrossOrigin
public class CartController extends BaseController {
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    CartRepository cartRepository;
    @Autowired
    TablesRepository tablesRepository;

    @Autowired
    BlacklistRepository blacklistRepository;

    @Autowired
    ShareCartRepository shareCartRepository;

    @ApiOperation(value="列表-获取指定桌子的已点菜品", notes="列表-获取指定桌子的已点菜品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "/table/{table_id}",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @PathVariable(value = "table_id")Long table_id,
            @RequestParam(value = "keyword",defaultValue = "")String keyword,
            @RequestParam(value = "share_pwd",defaultValue = "")String share_pwd,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Consumer consumer = (Consumer) request.getAttribute("user");
        Tables table = tablesRepository.findById(table_id).orElse(null);
        if(table == null){
            msgVo.setCode(40001);
            msgVo.setMsg("餐桌不存在");
            return msgVo;
        }

        Criteria<Cart> cartCriteria = new Criteria<>();
        if(!consumer.getWxOpenId().equals("-1")) {//一般是店内平板电脑
            // 更新当前用户使用的餐桌
            cartCriteria.add(Restrictions.eq("tables", table));
        }

        String pwdKey = "share_cart_consumer_id"+table.getStore().getId()+"-"+consumer.getUser().getId();
        TmpCache tmpCache = tmpCacheRepository.findOneByCKey(pwdKey);
        if(tmpCache != null){//如果原来有过，就用原来的
            share_pwd = tmpCache.getCValue();
        }


        ShareCart shareCart = null;
        if(StringUtils.isNotEmpty(share_pwd)){//用密码来获取
            if(tmpCache == null){
                TmpCache tmpCache1 = new TmpCache();
                tmpCache1.setCKey(pwdKey);
                tmpCache1.setCValue(share_pwd);
                tmpCache1.setExpireTime(3600*1000*24);
                tmpCacheRepository.saveAndFlush(tmpCache1);
            }
            Criteria<ShareCart> shareCartCriteria = new Criteria<>();
            shareCartCriteria.add(Restrictions.eq("tables", table));
            shareCartCriteria.add(Restrictions.eq("store", table.getStore()));
            shareCartCriteria.add(Restrictions.eq("sharePassword", share_pwd));
            shareCart = shareCartRepository.findOne(shareCartCriteria).orElse(null);
            if(shareCart == null){//为空时创建一个
                shareCart = new ShareCart();
                shareCart.setConsumer(consumer);
                shareCart.setSharePassword(share_pwd);
                shareCart.setStore(table.getStore());
                shareCart.setTables(table);
                shareCart.getConsumerList().add(consumer);
                shareCart = shareCartRepository.saveAndFlush(shareCart);
            }
            if(!shareCart.getConsumerList().contains(consumer)) {
                shareCart.getConsumerList().add(consumer);
                shareCart = shareCartRepository.saveAndFlush(shareCart);

                //推送通知
                if(shareCart != null) {//推送给大家
                    List<Consumer> consumers = shareCart.getConsumerList();
                    if(consumers.size() > 0) {
                        Long nowCid = consumer.getId();
                        for (Consumer consumer1 : consumers) {
                            if(consumer1.getId() == nowCid){//不需要推给自己
                                continue;
                            }
                            try {//向店家推送通知
                                Map<String, Object> map = new HashMap<>();
                                map.put("action", "join_share_cart");
                                map.put("user", consumer);
                                WebSocketServer.sendInfo("" + consumer1.getUser().getId(), WebsocketTypeEnum.SHARE_CART_SERVICE.getIndex(), map);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            cartCriteria.add(Restrictions.eq("shareCart", shareCart));
        }else {
            cartCriteria.add(Restrictions.eq("store", table.getStore()));
            cartCriteria.add(Restrictions.eq("consumer", consumer));
        }

        List<Cart> carts = cartRepository.findAll(cartCriteria);//把其他地方的购物车桌子变更为当前的桌子
        Map<Long,Cart> cartNumMap = new HashMap<>();
        if (carts.size() > 0) {
            for (Cart cart : carts) {
                cartNumMap.put(cart.getGoods().getId(),cart);
                if(cart.getTables().getId() != table_id) {
                    cart.setTables(table);
                    cartRepository.save(cart);
                }
            }
        }


        //菜品信息
        Criteria<Goods> goodsCriteria = new Criteria<>();
        goodsCriteria.add(Restrictions.eq("store",table.getStore()));
        goodsCriteria.add(Restrictions.eq("status",2));
        goodsCriteria.add(Restrictions.eq("outsideStatus",1));

        goodsCriteria.add(Restrictions.eq("isDelete",false));
        if(!keyword.trim().equals("")){
            goodsCriteria.add(Restrictions.or(Restrictions.like("name","%"+keyword+"%"),Restrictions.like("number","%"+keyword+"%")));
        }
        List<Goods> goods = goodsRepository.findAll(goodsCriteria);

        if(cartNumMap.size() > 0 && goods.size() > 0){
            for (Goods goods1:goods){
                Long gid = goods1.getId();
                if(cartNumMap.containsKey(gid)){
                    goods1.setCart_num(cartNumMap.get(gid).getNum());
                    goods1.setNote(cartNumMap.get(gid).getNote());
                }else{
                    goods1.setNote("");
                }
            }
        }
        msgVo.getData().put("user",consumer);
        msgVo.getData().put("share_cart",shareCart);
        msgVo.getData().put("goods",goods);
        msgVo.getData().put("carts",carts);
        msgVo.setMsg("获取成功");
        return msgVo;
    }

    @ApiOperation(value="变更购物车中的菜品", notes="变更购物车中的菜品")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/table/{table_id}/goods/{goods_id}",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo delete(@PathVariable("table_id")Long table_id,
                        @PathVariable("goods_id")Long goods_id,
                        @RequestParam("num")int num,@RequestParam(value = "note",defaultValue = "")String note
    ){
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
        Tables tables = tablesRepository.findById(table_id).orElse(null);
        if(tables == null){
            msgVo.setCode(40001);
            msgVo.setMsg("餐桌不存在");
            return msgVo;
        }
        Goods goods = goodsRepository.findById(goods_id).orElse(null);
        if(goods == null || goods.getStore().getId() != tables.getStore().getId() || goods.getStatus() ==1){
            msgVo.setCode(40002);
            msgVo.setMsg("菜品不存在或已下架");
            return msgVo;
        }

        Criteria<Cart> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",tables.getStore()));
        criteria.add(Restrictions.eq("goods",goods));
        if(consumer.getWxOpenId().equals("-1")) {//零时帐号购物车，通过桌子区分
            criteria.add(Restrictions.eq("tables", tables));
        }

        String share_pwd = null;

        String pwdKey = "share_cart_consumer_id"+tables.getStore().getId()+"-"+consumer.getUser().getId();
        TmpCache tmpCache = tmpCacheRepository.findOneByCKey(pwdKey);
        if(tmpCache != null){
            share_pwd = tmpCache.getCValue();
        }

        ShareCart shareCart = null;
        if(StringUtils.isNotEmpty(share_pwd)) {//用密码来获取
            Criteria<ShareCart> shareCartCriteria = new Criteria<>();
            shareCartCriteria.add(Restrictions.eq("tables", tables));
            shareCartCriteria.add(Restrictions.eq("store", tables.getStore()));
            shareCartCriteria.add(Restrictions.eq("sharePassword", share_pwd));
            shareCart = shareCartRepository.findOne(shareCartCriteria).orElse(null);
            if(shareCart == null){
                msgVo.setCode(40002);
                msgVo.setMsg("没有对应的一起点餐购物车");
                return msgVo;
            }
            criteria.add(Restrictions.eq("shareCart", shareCart));
        }else {
            criteria.add(Restrictions.eq("consumer", consumer));
        }
        Cart carts = cartRepository.findOne(criteria).orElse(null);
        if(num > 0){//增加的
            if(carts == null){//创建
                Cart cart = new Cart();
                cart.setTables(tables);
                cart.setGoods(goods);
                cart.setNum(num);
                cart.setNote(note);
                cart.setStore(tables.getStore());
                cart.setConsumer(consumer);
                cart.setShareCart(shareCart);
                carts = cartRepository.saveAndFlush(cart);
            }else{
                carts.setNum(num);
                carts.setStore(tables.getStore());
                carts.setConsumer(consumer);
                carts.setNote(note);
                cartRepository.save(carts);
            }
            //saveStoreLog(storeUser.getStore(),storeUser,"分店店员"+storeUser.getName()+"向购物车餐桌"+tables.getNumber()+"中增加菜品"+num+"份"+goods.getName());
        }else{
            cartRepository.delete(carts);
            //saveStoreLog(storeUser.getStore(),storeUser,"分店店员"+storeUser.getName()+"删除购物车餐桌"+tables.getNumber()+"中删除菜品"+num+"份"+goods.getName());
        }
        //推送通知
        if(shareCart != null) {//推送给大家
            List<Consumer> consumers = shareCart.getConsumerList();
            if(consumers.size() > 0) {
                Long nowCid = consumer.getId();
                for (Consumer consumer1 : consumers) {
                    if(consumer1.getId() == nowCid){//不需要推给自己
                        continue;
                    }
                    try {//向店家推送通知
                        Map<String, Object> map = new HashMap<>();
                        map.put("cart", carts);
                        map.put("action", "add_goods");
                        WebSocketServer.sendInfo("" + consumer1.getUser().getId(), WebsocketTypeEnum.SHARE_CART_SERVICE.getIndex(), map);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return msgVo;
    }


    @ApiOperation(value="退出共享点餐", notes="退出共享点餐")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/table/{table_id}/share_cart",method = RequestMethod.DELETE)
    @ResponseBody
    public MsgVo exitShareCart(@PathVariable("table_id")Long table_id) {
        MsgVo msgVo = new MsgVo();

        Consumer consumer = (Consumer) request.getAttribute("user");

        Tables table = tablesRepository.findById(table_id).orElse(null);
        if(table == null){
            msgVo.setCode(40001);
            msgVo.setMsg("餐桌不存在");
            return msgVo;
        }

        TmpCache tmpCache = tmpCacheRepository.findOneByCKey("share_cart_consumer_id"+table.getStore().getId()+"-"+consumer.getUser().getId());
        if(tmpCache == null){
            return msgVo;
        }
        String share_pwd = tmpCache.getCValue();

        Criteria<ShareCart> shareCartCriteria = new Criteria<>();
        shareCartCriteria.add(Restrictions.eq("store", consumer.getStore()));
        shareCartCriteria.add(Restrictions.eq("sharePassword", share_pwd));
        List<ShareCart> shareCarts = shareCartRepository.findAll(shareCartCriteria);
        if(shareCarts == null || shareCarts.size() == 0){
            return msgVo;
        }
        Long cid = consumer.getId();
        for(ShareCart shareCart:shareCarts){
            List<Consumer> consumers = shareCart.getConsumerList();
            if(consumers.size() > 1) {
                List<Consumer> delC = new ArrayList<>();
                for (Consumer consumer1 : consumers) {
                    if (consumer1.getId() == cid) {
                        delC.add(consumer);
                        break;
                    }
                }
                if(delC.size() > 0) {

                    if(consumers.size() > 0) {
                        Long nowCid = consumer.getId();
                        for (Consumer consumer1 : consumers) {
                            if(consumer1.getId() == nowCid){//不需要推给自己
                                continue;
                            }
                            try {//向店家推送通知
                                Map<String, Object> map = new HashMap<>();
                                map.put("action", "exit_share_cart");
                                map.put("user", consumer);
                                WebSocketServer.sendInfo("" + consumer1.getUser().getId(), WebsocketTypeEnum.SHARE_CART_SERVICE.getIndex(), map);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    consumers.remove(delC.get(0));
                    tmpCacheRepository.delete(tmpCache);
                }
            }else{//没有人时要清空
                if(consumers.size() == 0 || consumers.get(0).getId() == cid) {
                    Criteria<Cart> cartCriteria = new Criteria<>();
                    cartCriteria.add(Restrictions.eq("store", consumer.getStore()));
                    cartCriteria.add(Restrictions.eq("shareCart", shareCart));
                    List<Cart> carts = cartRepository.findAll(cartCriteria);
                    cartRepository.deleteAll(carts);
                    shareCartRepository.delete(shareCart);
                    tmpCacheRepository.delete(tmpCache);

                    if(consumers.size() > 0) {
                        Long nowCid = consumer.getId();
                        for (Consumer consumer1 : consumers) {
                            if(consumer1.getId() == nowCid){//不需要推给自己
                                continue;
                            }
                            try {//向店家推送通知
                                Map<String, Object> map = new HashMap<>();
                                map.put("action", "del_share_cart");
                                map.put("user", consumer);
                                WebSocketServer.sendInfo("" + consumer1.getUser().getId(), WebsocketTypeEnum.SHARE_CART_SERVICE.getIndex(), map);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }


        }


        return msgVo;
    }

}

package com.food.order.controller.storeUser;


import com.alibaba.fastjson.JSON;
import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.*;
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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店员购物车管理", description = "店员购物车的接口",tags="店员-购物车接口")
@RestController("store_user_cart_controller")
@RequestMapping("/api/store_user/cart")
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

    @ApiOperation(value="列表-获取指定桌子的已点菜品", notes="列表-获取指定桌子的已点菜品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "table_id")Long table_id,
            @RequestParam(value = "keyword",defaultValue = "")String keyword,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        Criteria<Cart> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",storeUser.getStore()));
        criteria.add(Restrictions.eq("tables",tablesRepository.findById(table_id).orElse(null)));
        List<Cart> carts = cartRepository.findAll(criteria);
        Map<Long,Cart> cartNumMap = new HashMap<>();
        if(carts.size() > 0){
            for(Cart cart:carts){
                cartNumMap.put(cart.getGoods().getId(),cart);
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
    public MsgVo delete(@PathVariable("table_id")Long table_id,@PathVariable("goods_id")Long goods_id,@RequestParam("num")int num,@RequestParam(value = "note",defaultValue = "")String note){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");

        Tables tables = tablesRepository.findById(table_id).orElse(null);
        if(tables == null || tables.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("餐桌不存在");
            return msgVo;
        }
        Goods goods = goodsRepository.findById(goods_id).orElse(null);
        if(goods == null || goods.getStore().getId() != storeUser.getStore().getId() || goods.getStatus() ==1){
            msgVo.setCode(40002);
            msgVo.setMsg("菜品不存在或已下架");
            return msgVo;
        }

        Criteria<Cart> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",storeUser.getStore()));
        criteria.add(Restrictions.eq("tables",tables));
        criteria.add(Restrictions.eq("goods",goods));
        criteria.add(Restrictions.isNull("consumer"));
        Cart carts = cartRepository.findOne(criteria).orElse(null);
        if(num > 0){//增加的
            if(carts == null){//创建
                Cart cart = new Cart();
                cart.setTables(tables);
                cart.setGoods(goods);
                cart.setNum(num);
                cart.setNote(note);
                cart.setStore(storeUser.getStore());
                cartRepository.save(cart);
            }else{
                carts.setNum(num);
                carts.setStore(storeUser.getStore());
                carts.setNote(note);
                cartRepository.save(carts);
            }
            saveStoreLog(storeUser.getStore(),storeUser,"分店店员"+storeUser.getName()+"向购物车餐桌"+tables.getNumber()+"中增加菜品"+num+"份"+goods.getName());
        }else{
            cartRepository.delete(carts);
            saveStoreLog(storeUser.getStore(),storeUser,"分店店员"+storeUser.getName()+"删除购物车餐桌"+tables.getNumber()+"中删除菜品"+num+"份"+goods.getName());
        }
        return msgVo;
    }





    @ApiOperation(value="购物车变更桌子", notes="购物车变更桌子")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo updateTable(@RequestParam(value = "to_table_id")Long to_table_id,
                             @RequestParam(value = "from_table_id")Long from_table_id){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");

        Tables fromTables = tablesRepository.findById(from_table_id).orElse(null);
        if(fromTables == null || fromTables.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("当前餐桌不存在");
            return msgVo;
        }
        Tables toTtables = tablesRepository.findById(to_table_id).orElse(null);
        if(toTtables == null || toTtables.getStore().getId() != toTtables.getStore().getId()){
            msgVo.setCode(40003);
            msgVo.setMsg("目标餐桌不存在");
            return msgVo;
        }
        Criteria<Cart> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",storeUser.getStore()));
        criteria.add(Restrictions.eq("tables",fromTables));
        List<Cart> carts = cartRepository.findAll(criteria);
        if(carts.size() > 0) {
            for (Cart cart : carts) {
                cart.setTables(toTtables);
            }
            cartRepository.saveAll(carts);
        }

        fromTables.setStatus(1);
        toTtables.setStatus(2);

        tablesRepository.save(fromTables);
        tablesRepository.save(toTtables);
        saveStoreLog(storeUser.getStore(),storeUser,"分店店员"+storeUser.getName()+"修改购物车中的桌号"+fromTables.getNumber()+"为"+toTtables.getNumber());

        return msgVo;
    }

}

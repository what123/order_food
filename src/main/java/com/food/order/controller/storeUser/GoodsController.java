package com.food.order.controller.storeUser;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.GoodsRepository;
import com.food.order.model.repository.GoodsTagRepository;
import com.food.order.model.repository.StoreRepository;
import com.food.order.model.repository.UploadFileRepository;
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
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店员菜品管理", description = "店员菜品的接口",tags="店员-菜品接口")
@RestController("store_user_goods_controller")
@RequestMapping("/api/store_user/goods")
@CrossOrigin
public class GoodsController extends BaseController {
    @Autowired
    GoodsRepository goodsRepository;



    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "goods_id",defaultValue = "0")Long goods_id,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        Criteria<Goods> criteria = new Criteria<>();
        if(goods_id > 0){
            criteria.add(Restrictions.eq("id",goods_id));
        }
        criteria.add(Restrictions.eq("store",storeUser.getStore()));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<Goods> goods = goodsRepository.findAll(criteria,pageable);
        msgVo.getData().put("goods",goods);
        msgVo.setMsg("获取成功");
        return msgVo;
    }

}

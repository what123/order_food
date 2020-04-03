package com.food.order.controller.mainStore;


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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "店家菜品管理", description = "店家菜品的接口",tags="店家-菜品接口")
@RestController("main_store_goods_controller")
@RequestMapping("/api/main_store/goods")
@CrossOrigin
public class GoodsController extends BaseController {
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    GoodsTagRepository goodsTagRepository;
    @Autowired
    UploadFileRepository uploadFileRepository;



    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "keyword",defaultValue = "")String keyword,
            @RequestParam(value = "goods_id",defaultValue = "0")Long goods_id,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");
        Criteria<Goods> criteria = new Criteria<>();
        if(goods_id > 0){
            criteria.add(Restrictions.eq("id",goods_id));
        }
        if(!keyword.trim().equals("")){
            criteria.add(Restrictions.or(Restrictions.like("name","%"+keyword+"%"),Restrictions.like("number","%"+keyword+"%")));
        }


        criteria.add(Restrictions.eq("mainStore",mainStore));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<Goods> goods = goodsRepository.findAll(criteria,pageable);
        msgVo.getData().put("goods",goods);
        msgVo.setMsg("获取成功");
        return msgVo;
    }

    @ApiOperation(value="创建/修改", notes="创建/修改")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo add(@RequestParam(value = "id",defaultValue = "0")Long id,
                     @RequestParam("name")String name,
                     @RequestParam("number")String number,
                     @RequestParam("marker_price")Integer marker_price,
                     @RequestParam("sell_price")Integer sell_price,
                     @RequestParam("note")String note,
                     @RequestParam(value = "tag_ids",defaultValue = "")String tag_ids,
                     @RequestParam(value = "printer_ids",defaultValue = "")String printer_ids,
                     @RequestParam(value = "pic_ids",defaultValue = "")String pic_ids,
                     @RequestParam(value = "status")int status,
                     @RequestParam(value = "outsideStatus")int outsideStatus

    ){
        MsgVo msgVo = new MsgVo();
        MainStore mainStore = (MainStore) request.getAttribute("user");
        Goods goods = null;
        if(id > 0){
            goods = goodsRepository.findById(id).orElse(null);
            if(goods == null || goods.getMainStore().getId() != mainStore.getId()){
                msgVo.setCode(40002);
                msgVo.setMsg("菜品不存在");
                return msgVo;
            }
        }else{
            goods = new Goods();
            goods.setMainStore(mainStore);

        }
        //number去重
        Criteria<Goods> goodsCriteria = new Criteria<>();
        goodsCriteria.add(Restrictions.eq("number",number));
        goodsCriteria.add(Restrictions.eq("mainStore",mainStore));
        goodsCriteria.add(Restrictions.eq("isDelete",false));
        Goods goods1 = goodsRepository.findOne(goodsCriteria).orElse(null);
        if(goods1 != null && goods1.getId() != id){
            msgVo.setCode(40003);
            msgVo.setMsg("菜品编号已存在,请换一个编号");
            return msgVo;
        }

        goods.setName(name);
        goods.setNumber(number);
        goods.setMarker_price(marker_price);
        goods.setSell_price(sell_price);
        goods.setNote(note);
        goods.setStatus(status);
        goods.setOutsideStatus(outsideStatus);
        if(StringUtils.isNotEmpty(tag_ids.trim())){
            String[] tagIds = tag_ids.split("#");
            List<String> tIds = Arrays.asList(tagIds);
            Criteria<GoodsTag> criteria = new Criteria<>();
            criteria.add(Restrictions.in("id", tIds));
            List<GoodsTag> goodsTags = goodsTagRepository.findAll(criteria);
            goods.setGoodsTags(goodsTags);
        }
        if(StringUtils.isNotEmpty(printer_ids.trim())){
            String[] printerIds = printer_ids.split("#");
            List<String> pIds = Arrays.asList(printerIds);
            Criteria<Printer> criteria = new Criteria<>();
            criteria.add(Restrictions.in("id", pIds));
            List<Printer> printers = printerRepository.findAll(criteria);
            goods.setPrinters(printers);
        }

        if(StringUtils.isNotEmpty(pic_ids.trim())){
            String[] fids = pic_ids.split("#");
            List<String> fidList = Arrays.asList(fids);
            Criteria<UploadFile> uploadFileCriteria = new Criteria<>();
            uploadFileCriteria.add(Restrictions.in("id",fidList));
            List<UploadFile> uploadFiles = uploadFileRepository.findAll(uploadFileCriteria);
            goods.setPics(uploadFiles);
        }
        goodsRepository.save(goods);
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
        Goods goods = goodsRepository.findById(id).orElse(null);
        if(goods == null || goods.getMainStore().getId() != mainStore.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("菜品不存在");
            return msgVo;
        }
        goodsRepository.delete(goods);
        return msgVo;
    }

}

package com.food.order.controller.store;


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
import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "分店菜品管理", description = "分店菜品的接口",tags="分店-菜品接口")
@RestController("store_goods_controller")
@RequestMapping("/api/store/goods")
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
        Store store = (Store) request.getAttribute("user");
        Criteria<Goods> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",store));
        if(goods_id > 0){
            criteria.add(Restrictions.eq("id",goods_id));
        }

        if(!keyword.trim().equals("")){
            criteria.add(Restrictions.or(Restrictions.like("name","%"+keyword+"%"),Restrictions.like("number","%"+keyword+"%")));
        }

        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<Goods> goods = goodsRepository.findAll(criteria,pageable);
        msgVo.getData().put("goods",goods);
        msgVo.setMsg("获取成功");
        return msgVo;
    }

    @ApiOperation(value="店家菜品列表", notes="店家菜品列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "/main_goods",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo main_goods(
            @RequestParam(value = "keyword",defaultValue = "")String keyword,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");


        Criteria<Goods> criteria = new Criteria<>();
        if(!keyword.trim().equals("")){
            criteria.add(Restrictions.or(Restrictions.like("name","%"+keyword+"%"),Restrictions.like("number","%"+keyword+"%")));
        }
        criteria.add(Restrictions.eq("mainStore",store.getMainStore()));
        criteria.add(Restrictions.eq("status",2));
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
                     @RequestParam(value = "kitchenPrinter")int kitchenPrinter,
                     @RequestParam(value = "outsideStatus")int outsideStatus

    ){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");

        Goods goods = null;
        if(id > 0){
            goods = goodsRepository.findById(id).orElse(null);
            if(goods == null || goods.getStore().getId() != store.getId()){
                msgVo.setCode(40002);
                msgVo.setMsg("菜品不存在");
                return msgVo;
            }
        }else{
            goods = new Goods();
            goods.setStore(store);
        }

        //number去重
        Criteria<Goods> goodsCriteria = new Criteria<>();
        goodsCriteria.add(Restrictions.eq("number",number));
        goodsCriteria.add(Restrictions.eq("store",store));
        goodsCriteria.add(Restrictions.eq("isDelete",false));
        Goods goods1 = goodsRepository.findOne(goodsCriteria).orElse(null);
        if(goods1 != null && goods1.getId() != id){
            msgVo.setCode(40003);
            msgVo.setMsg("菜品编号已存在,请换一个编号");
            return msgVo;
        }
        goods.setKitchenPrinter(kitchenPrinter);
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
        }else{
            goods.getGoodsTags().clear();
        }
        if(StringUtils.isNotEmpty(printer_ids.trim())){
            String[] printerIds = printer_ids.split("#");
            List<String> pIds = Arrays.asList(printerIds);
            Criteria<Printer> criteria = new Criteria<>();
            criteria.add(Restrictions.in("id", pIds));
            List<Printer> printers = printerRepository.findAll(criteria);
            goods.setPrinters(printers);
        }else{
            goods.getPrinters().clear();
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

    @ApiOperation(value="复制总部的菜品", notes="复制总部的菜品")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/copy",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo copy(@RequestParam(value = "ids",defaultValue = "")String ids){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");

        Criteria<Goods> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",store));
        List<Goods> goodsList = goodsRepository.findAll(criteria);

        List<String> goodsNames = new ArrayList<>();
        for(Goods goods:goodsList){
            goodsNames.add(goods.getName());
        }

        if(StringUtils.isNotEmpty(ids.trim())){
            String[] Ids = ids.split("#");
            List<String> tIds = Arrays.asList(Ids);
            Criteria<Goods> goodsCriteria = new Criteria<>();
            goodsCriteria.add(Restrictions.in("id", tIds));
            goodsCriteria.add(Restrictions.eq("mainStore",store.getMainStore()));
            List<Goods> mainStoreGoods = goodsRepository.findAll(goodsCriteria);
            if(mainStoreGoods.size() > 0) {
                for (Goods goods : mainStoreGoods) {
                    if(goodsNames.contains(goods.getName())){//已经存在同名的就不增加了
                        continue;
                    }
                    Goods goods1 = new Goods();
                    goods1.setStore(store);
                    goods1.setName(goods.getName());
                    goods1.setNote(goods.getNote());
                    goods1.setNumber(goods.getNumber());
                    goods1.setSell_price(goods.getSell_price());
                    goods1.setMarker_price(goods.getMarker_price());
                    goods1.setStatus(1);
                    if(goods.getPics() != null && goods.getPics().size() > 0){
                        List<UploadFile> picss = new ArrayList<>();
                        List<UploadFile> pics = goods.getPics();
                        for (UploadFile pic:pics) {
                            picss.add(pic);
                        }
                        goods1.setPics(picss);
                    }
                    if(goods.getGoodsTags() != null && goods.getGoodsTags().size() > 0){
                        // 把不存在的tag复制过来
                        Criteria<GoodsTag> goodsTagCriteria = new Criteria<>();
                        goodsTagCriteria.add(Restrictions.eq("store",store));
                        List<GoodsTag> goodsTags = goodsTagRepository.findAll(goodsTagCriteria);

                        Map<String,GoodsTag> goodsTagMap = new HashMap<>();
                        for(GoodsTag goodsTag:goodsTags){
                            goodsTagMap.put(goodsTag.getName(),goodsTag);
                        }

                        List<GoodsTag> tagss = new ArrayList<>();
                        List<GoodsTag> tags = goods.getGoodsTags();
                        for (GoodsTag tag:tags) {
                            if(goodsTagMap.containsKey(tag.getName())){
                                tagss.add(goodsTagMap.get(tag.getName()));
                            }else{
                                GoodsTag goodsTag1 = new GoodsTag();
                                goodsTag1.setName(tag.getName());
                                goodsTag1.setStore(store);
                                if(tag.getPics() != null && tag.getPics().size() > 0){
                                    List<UploadFile> picss = new ArrayList<>();
                                    List<UploadFile> pics = tag.getPics();
                                    for (UploadFile pic:pics) {
                                        picss.add(pic);
                                    }
                                    goodsTag1.setPics(picss);
                                }
                                goodsTag1 = goodsTagRepository.saveAndFlush(goodsTag1);
                                tagss.add(goodsTag1);
                            }

                        }
                        goods1.setGoodsTags(tagss );
                    }
                    goodsRepository.save(goods1);
                }
            }
        }

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
        Goods goods = goodsRepository.findById(id).orElse(null);
        if(goods == null || goods.getStore().getId() != store.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("菜品不存在");
            return msgVo;
        }
        goodsRepository.delete(goods);
        return msgVo;
    }

}

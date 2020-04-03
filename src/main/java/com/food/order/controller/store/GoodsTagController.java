package com.food.order.controller.store;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.GoodsTagRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "分店菜品标签管理", description = "分店菜品标签的接口",tags="分店-菜品接口")
@RestController("store_goods_tag_controller")
@RequestMapping("/api/store/goods_tag")
@CrossOrigin
public class GoodsTagController extends BaseController {
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
            @RequestParam(value = "id",defaultValue = "0")Long id,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Criteria<GoodsTag> criteria = new Criteria<>();
        if(id > 0){
            criteria.add(Restrictions.eq("id",id));
        }
        criteria.add(Restrictions.eq("store",store));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<GoodsTag> goods = goodsTagRepository.findAll(criteria,pageable);
        msgVo.getData().put("goods_tags",goods);
        msgVo.setMsg("获取成功");
        return msgVo;
    }


    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "main_goods_tags",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo main_goods(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Criteria<GoodsTag> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("mainStore",store.getMainStore()));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<GoodsTag> goods = goodsTagRepository.findAll(criteria,pageable);
        msgVo.getData().put("goods_tags",goods);
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
                     @RequestParam(value = "pic_ids",defaultValue = "")String pic_ids
    ){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        User user = store.getUser();
        GoodsTag goodsTag = null;
        if(id > 0){
            goodsTag = goodsTagRepository.findById(id).orElse(null);
            if(goodsTag == null || goodsTag.getStore().getId() != store.getId()){
                msgVo.setCode(40002);
                msgVo.setMsg("菜品标签不存在");
                return msgVo;
            }
        }else{
            goodsTag = new GoodsTag();
            goodsTag.setStore(store);
        }
        if(StringUtils.isNotEmpty(pic_ids.trim())){
            String[] fids = pic_ids.split("#");
            List<String> fidList = Arrays.asList(fids);
            Criteria<UploadFile> uploadFileCriteria = new Criteria<>();
            uploadFileCriteria.add(Restrictions.in("id",fidList));
            List<UploadFile> uploadFiles = uploadFileRepository.findAll(uploadFileCriteria);
            goodsTag.setPics(uploadFiles);
        }

        goodsTag.setName(name);
        goodsTagRepository.save(goodsTag);
        return msgVo;
    }

    @ApiOperation(value="复制总部的菜品标签", notes="复制总部的菜品标签")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/copy",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo copy(@RequestParam(value = "tag_ids",defaultValue = "")String tag_ids){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");

        Criteria<GoodsTag> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("store",store));
        List<GoodsTag> goodsTags = goodsTagRepository.findAll(criteria);

        List<String> goodsTagNames = new ArrayList<>();
        for(GoodsTag goodsTag:goodsTags){
            goodsTagNames.add(goodsTag.getName());
        }

        if(StringUtils.isNotEmpty(tag_ids.trim())){
            String[] tagIds = tag_ids.split("#");
            List<String> tIds = Arrays.asList(tagIds);
            Criteria<GoodsTag> goodsTagCriteria = new Criteria<>();
            goodsTagCriteria.add(Restrictions.in("id", tIds));
            goodsTagCriteria.add(Restrictions.eq("mainStore",store.getMainStore()));
            List<GoodsTag> mainStoreGoodsTags = goodsTagRepository.findAll(goodsTagCriteria);
            if(mainStoreGoodsTags.size() > 0) {
                for (GoodsTag goodsTag : mainStoreGoodsTags) {
                    if(goodsTagNames.contains(goodsTag.getName())){//已经存在同名的就不增加了
                        continue;
                    }
                    GoodsTag goodsTag1 = new GoodsTag();
                    goodsTag1.setName(goodsTag.getName());
                    goodsTag1.setStore(store);
                    if(goodsTag.getPics() != null && goodsTag.getPics().size() > 0){
                        List<UploadFile> picss = new ArrayList<>();
                        List<UploadFile> pics = goodsTag.getPics();
                        for (UploadFile pic:pics) {
                            picss.add(pic);
                        }
                        goodsTag1.setPics(picss);
                    }
                    goodsTagRepository.save(goodsTag1);
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
        GoodsTag goodsTag = goodsTagRepository.findById(id).orElse(null);
        if(goodsTag == null || goodsTag.getStore().getId() != store.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("菜品标签不存在");
            return msgVo;
        }
        goodsTagRepository.delete(goodsTag);
        return msgVo;
    }

}

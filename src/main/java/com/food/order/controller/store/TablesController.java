package com.food.order.controller.store;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.config.Config;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.StoreUserRepository;
import com.food.order.model.repository.TablesRepository;
import com.food.order.model.repository.UserRepository;
import com.food.order.utils.utils.MD5Util;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "分店餐桌管理", description = "分店餐桌的接口",tags="分店-餐桌接口")
@RestController("store_tables_controller")
@RequestMapping("/api/store/tables")
@CrossOrigin
public class TablesController extends BaseController {
    @Autowired
    TablesRepository tablesRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreUserRepository storeUserRepository;



    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页行数,默认10条", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    public MsgVo list(
            @RequestParam(value = "id",defaultValue = "0")Long id,
            @RequestParam(value = "keyword",defaultValue = "")String keyword,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
            HttpServletRequest request){

        MsgVo msgVo =new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Criteria<Tables> criteria = new Criteria<>();
        if(id > 0){
            criteria.add(Restrictions.eq("id",id));
        }
        if(!keyword.trim().equals("")){
            criteria.add(Restrictions.or(Restrictions.like("name","%"+keyword+"%"),Restrictions.like("number","%"+keyword+"%")));
        }
        criteria.add(Restrictions.eq("store",store));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<Tables> tables = tablesRepository.findAll(criteria,pageable);
        if(tables.getContent() != null && tables.getContent().size() > 0){
            for(Tables tables1:tables){
                try {
                    tables1.setQrPic(URLEncoder.encode(Config.host+"/table/"+tables1.getUuid(),"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        msgVo.getData().put("tables",tables);
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
                     @RequestParam("minPeopleCount")int minPeopleCount,
                     @RequestParam("maxPeopleCount")int maxPeopleCount,
                     @RequestParam(value = "storeUserNumber",defaultValue = "")String storeUserNumber,
                     @RequestParam("note")String note

    ){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Tables tables = null;
        if(id > 0){
            tables = tablesRepository.findById(id).orElse(null);
            if(tables == null || tables.getStore().getId() != store.getId()){
                msgVo.setCode(40001);
                msgVo.setMsg("餐桌不存在");
                return msgVo;
            }
        }else{
            tables = new Tables();
            try {
                tables.setUuid(MD5Util.MD5Encode(""+store.getId()+number+System.currentTimeMillis()));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        tables.setMinPeopleCount(minPeopleCount);
        tables.setMaxPeopleCount(maxPeopleCount);
        tables.setStore(store);
        tables.setName(name);
        tables.setNumber(number);
        tables.setNote(note);

        if(!storeUserNumber.equals("")){
            Criteria<StoreUser> criteria = new Criteria<>();
            criteria.add(Restrictions.eq("store",store));
            criteria.add(Restrictions.eq("number",storeUserNumber));
            StoreUser storeUser = storeUserRepository.findOne(criteria).orElse(null);
            if(storeUser != null){
                tables.setStoreUser(storeUser);
            }else{
                msgVo.setCode(40002);
                msgVo.setMsg("该店员编号不存在,请输入正确的店员编号");
                return msgVo;
            }
        }else{
            tables.setStoreUser(null);
        }

        tablesRepository.save(tables);

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
        Tables tables = tablesRepository.findById(id).orElse(null);
        if(tables == null || tables.getStore().getId() != store.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("餐桌不存在");
            return msgVo;
        }
        tablesRepository.delete(tables);
        return msgVo;
    }


    @ApiOperation(value="释放餐桌", notes="释放餐桌")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/unused/{id}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo unused(@PathVariable("id")Long id,@RequestParam(value = "status",defaultValue = "1")int status){
        MsgVo msgVo = new MsgVo();
        Store store = (Store) request.getAttribute("user");
        Tables tables = tablesRepository.findById(id).orElse(null);
        if(tables == null || tables.getStore().getId() != store.getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("餐桌不存在");
            return msgVo;
        }
        tables.setStatus(status);
        tablesRepository.save(tables);
        return msgVo;
    }
}

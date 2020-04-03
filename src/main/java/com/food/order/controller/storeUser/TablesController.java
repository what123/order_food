package com.food.order.controller.storeUser;


import com.food.order.controller.BaseController;
import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.TablesRepository;
import com.food.order.model.repository.UserRepository;
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
@Api( value = "分店员餐桌管理", description = "分店员餐桌的接口",tags="店员-餐桌接口")
@RestController("store_user_tables_controller")
@RequestMapping("/api/store_user/tables")
@CrossOrigin
public class TablesController extends BaseController {
    @Autowired
    TablesRepository tablesRepository;
    @Autowired
    UserRepository userRepository;



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
        StoreUser storeUser = (StoreUser) request.getAttribute("user");

        Criteria<Tables> criteria = new Criteria<>();
        if(id > 0){
            criteria.add(Restrictions.eq("id",id));
        }
        if(!keyword.trim().equals("")){
            criteria.add(Restrictions.or(Restrictions.like("name","%"+keyword+"%"),Restrictions.like("number","%"+keyword+"%")));
        }
        criteria.add(Restrictions.eq("store",storeUser.getStore()));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable =  new PageRequest(page-1, pageSize, sort);
        Page<Tables> tables = tablesRepository.findAll(criteria,pageable);
        msgVo.getData().put("tables",tables);
        msgVo.setMsg("获取成功");
        return msgVo;
    }

    @ApiOperation(value="并桌", notes="并桌")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/assemble_table",method = RequestMethod.POST)
    @ResponseBody
    public MsgVo add(@RequestParam(value = "from_table_id")Long id1,
                     @RequestParam(value = "to_table_id")Long id2,
                     @RequestParam(value = "note",defaultValue = "")String note
                     ){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        Tables tables1 = tablesRepository.findById(id1).orElse(null);
        if(tables1 == null || tables1.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40001);
            msgVo.setMsg("餐桌1不存在");
            return msgVo;
        }
        Tables tables2 = tablesRepository.findById(id1).orElse(null);
        if(tables2 == null || tables2.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("餐桌2不存在");
            return msgVo;
        }
        Tables assembleTable = null;
        if(tables1.isAssemble()){
            assembleTable = tables1;
        }else if(tables2.isAssemble()){
            assembleTable = tables2;
        }

        if(assembleTable == null){//没有并过桌子，需要创建并桌
            assembleTable = new Tables();
            assembleTable.setName(tables1.getName()+"#"+tables2.getName());
            assembleTable.setNumber(tables1.getNumber()+"#"+tables2.getNumber());
            assembleTable.setStatus(2);
            assembleTable.setStore(storeUser.getStore());
            assembleTable.setAssemble(true);
            assembleTable = tablesRepository.saveAndFlush(assembleTable);
        }else{
            assembleTable.setName(tables1.getName()+"#"+tables2.getName());
            assembleTable.setNumber(tables1.getNumber()+"#"+tables2.getNumber());
            assembleTable.setStatus(2);
            assembleTable.setAssemble(true);
            assembleTable = tablesRepository.saveAndFlush(assembleTable);
        }

        if(tables1.isAssemble() && assembleTable.getId() != tables1.getId()){
            tablesRepository.delete(tables1);
        }
        if(tables2.isAssemble()  && assembleTable.getId() != tables2.getId()){
            tablesRepository.delete(tables2);
        }

        if(!tables1.isAssemble()){
            tables1.setStatus(2);
            tablesRepository.save(tables1);
        }
        if(!tables2.isAssemble()){
            tables2.setStatus(2);
            tablesRepository.save(tables2);
        }
        return msgVo;
    }

    @ApiOperation(value="删除拼桌", notes="删除拼桌")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/assemble_table/{id}",method = RequestMethod.DELETE)
    @ResponseBody
    public MsgVo add(@PathVariable(value = "id")Long id){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        Criteria<Tables> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("id", id));
        criteria.add(Restrictions.eq("status", 2));
        criteria.add(Restrictions.eq("store", storeUser.getStore()));
        criteria.add(Restrictions.eq("isDelete", false));
        criteria.add(Restrictions.eq("isAssemble", true));

        Tables tables = tablesRepository.findOne(criteria).orElse(null);
        if(tables == null || tables.getStore().getId() != storeUser.getStore().getId()){
            msgVo.setCode(40002);
            msgVo.setMsg("餐桌不存在");
            return msgVo;
        }

        //还原拼桌的原桌子的状态
        String numbers = tables.getNumber();
        String[] tableNumbers = numbers.split("#");
        List<String> tNumbers = Arrays.asList(tableNumbers);
        Criteria<Tables> tablesCriteria = new Criteria<>();
        tablesCriteria.add(Restrictions.in("number", tNumbers));
        tablesCriteria.add(Restrictions.eq("status", 2));
        tablesCriteria.add(Restrictions.eq("store", storeUser.getStore()));
        tablesCriteria.add(Restrictions.eq("isDelete", false));
        List<Tables> tables2 = tablesRepository.findAll(criteria);
        for(Tables tables1:tables2){
            tables1.setStatus(1);
        }
        tablesRepository.saveAll(tables2);
        tablesRepository.delete(tables);

        return msgVo;
    }

    @ApiOperation(value="修改桌子使用状态", notes="修改桌子使用状态")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    @ResponseBody
    public MsgVo delete(@PathVariable("id")Long id,@RequestParam(value = "status",defaultValue = "1")int status){
        MsgVo msgVo = new MsgVo();
        StoreUser storeUser = (StoreUser) request.getAttribute("user");
        Tables tables = tablesRepository.findById(id).orElse(null);
        if(tables == null){
            msgVo.setCode(40001);
            msgVo.setMsg("餐桌不存在");
            return msgVo;
        }
        tables.setStatus(status);
        tablesRepository.save(tables);
        return msgVo;
    }

}

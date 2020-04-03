package com.food.order.model.service;

import com.food.order.model.MsgVo;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.MainStore;
import com.food.order.model.entity.Orders;
import com.food.order.model.entity.Plugins;
import com.food.order.model.entity.Store;
import com.food.order.model.repository.OrdersRepository;
import com.food.order.model.repository.PluginsRepository;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.PluginsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdersServiceImpl {
    @Autowired
    OrdersRepository ordersRepository;

    public int getOutNo(Store store){
        int outNo = 1101;//从1101开始
        Criteria<Orders> ordersCriteria = new Criteria<>();
        ordersCriteria.add(Restrictions.eq("store",store));
        ordersCriteria.add(Restrictions.eq("applyStatus",2));
        Sort sort = new Sort(Sort.Direction.DESC, "outNo");
        Pageable pageable =  new PageRequest(0, 1, sort);
        Page<Orders> orders = ordersRepository.findAll(ordersCriteria,pageable);
        if(orders.getContent() != null && orders.getContent().size() > 0){
            outNo += 1;
        }
        return outNo;
    }
}

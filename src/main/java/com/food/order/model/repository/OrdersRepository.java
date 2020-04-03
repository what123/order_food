package com.food.order.model.repository;


import com.alibaba.fastjson.JSON;
import com.food.order.model.entity.Goods;
import com.food.order.model.entity.OrderGoods;
import com.food.order.model.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrdersRepository extends JpaSpecificationExecutor<Orders>,JpaRepository<Orders, Long>,PagingAndSortingRepository<Orders, Long> {



}
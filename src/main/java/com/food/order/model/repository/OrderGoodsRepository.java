package com.food.order.model.repository;


import com.food.order.model.entity.OrderGoods;
import com.food.order.model.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderGoodsRepository extends JpaSpecificationExecutor<OrderGoods>,JpaRepository<OrderGoods, Long>,PagingAndSortingRepository<OrderGoods, Long> {


}
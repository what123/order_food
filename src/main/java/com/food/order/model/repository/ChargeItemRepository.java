package com.food.order.model.repository;


import com.food.order.model.entity.BillLog;
import com.food.order.model.entity.ChargeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ChargeItemRepository extends JpaSpecificationExecutor<ChargeItem>,JpaRepository<ChargeItem, Long>,PagingAndSortingRepository<ChargeItem, Long> {


}
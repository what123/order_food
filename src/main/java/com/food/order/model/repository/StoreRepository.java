package com.food.order.model.repository;


import com.food.order.model.entity.BillLog;
import com.food.order.model.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StoreRepository extends JpaSpecificationExecutor<Store>,JpaRepository<Store, Long>,PagingAndSortingRepository<Store, Long> {


}
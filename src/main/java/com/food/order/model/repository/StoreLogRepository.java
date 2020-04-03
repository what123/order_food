package com.food.order.model.repository;


import com.food.order.model.entity.StoreLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StoreLogRepository extends JpaSpecificationExecutor<StoreLog>,JpaRepository<StoreLog, Long>,PagingAndSortingRepository<StoreLog, Long> {


}
package com.food.order.model.repository;


import com.food.order.model.entity.BillLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BillLogRepository extends JpaSpecificationExecutor<BillLog>,JpaRepository<BillLog, Long>,PagingAndSortingRepository<BillLog, Long> {


}
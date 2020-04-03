package com.food.order.model.repository;


import com.food.order.model.entity.CallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CallLogRepository extends JpaSpecificationExecutor<CallLog>,JpaRepository<CallLog, Long>,PagingAndSortingRepository<CallLog, Long> {


}
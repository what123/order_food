package com.food.order.model.repository;


import com.food.order.model.entity.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2018/4/2.
 */

@Repository
public interface ConsumerRepository extends JpaSpecificationExecutor<Consumer>,JpaRepository<Consumer, Long>,PagingAndSortingRepository<Consumer, Long> {


}
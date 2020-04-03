package com.food.order.model.repository;


import com.food.order.model.entity.Goods;
import com.food.order.model.entity.Tables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TablesRepository extends JpaSpecificationExecutor<Tables>,JpaRepository<Tables, Long>,PagingAndSortingRepository<Tables, Long> {


}
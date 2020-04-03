package com.food.order.model.repository;


import com.food.order.model.entity.BillLog;
import com.food.order.model.entity.GamesConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GamesConfigRepository extends JpaSpecificationExecutor<GamesConfig>,JpaRepository<GamesConfig, Long>,PagingAndSortingRepository<GamesConfig, Long> {


}
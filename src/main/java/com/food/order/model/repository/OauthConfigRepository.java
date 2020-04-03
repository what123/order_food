package com.food.order.model.repository;


import com.food.order.model.entity.Goods;
import com.food.order.model.entity.OauthConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OauthConfigRepository extends JpaSpecificationExecutor<OauthConfig>,JpaRepository<OauthConfig, Long>,PagingAndSortingRepository<OauthConfig, Long> {


}
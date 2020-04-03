package com.food.order.model.repository;


import com.food.order.model.entity.VipUserCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VipUserCardRepository extends JpaSpecificationExecutor<VipUserCard>,JpaRepository<VipUserCard, Long>,PagingAndSortingRepository<VipUserCard, Long> {


}
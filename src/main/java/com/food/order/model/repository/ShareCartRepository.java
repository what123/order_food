package com.food.order.model.repository;


import com.food.order.model.entity.ShareCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ShareCartRepository extends JpaSpecificationExecutor<ShareCart>,JpaRepository<ShareCart, Long>,PagingAndSortingRepository<ShareCart, Long> {


}
package com.food.order.model.repository;


import com.food.order.model.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartRepository extends JpaSpecificationExecutor<Cart>,JpaRepository<Cart, Long>,PagingAndSortingRepository<Cart, Long> {


}
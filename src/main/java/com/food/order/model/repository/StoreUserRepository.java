package com.food.order.model.repository;


import com.food.order.model.entity.Store;
import com.food.order.model.entity.StoreUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StoreUserRepository extends JpaSpecificationExecutor<StoreUser>,JpaRepository<StoreUser, Long>,PagingAndSortingRepository<StoreUser, Long> {


}
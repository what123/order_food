package com.food.order.model.repository;


import com.food.order.model.entity.MainStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MainStoreRepository extends JpaSpecificationExecutor<MainStore>,JpaRepository<MainStore, Long>,PagingAndSortingRepository<MainStore, Long> {


}
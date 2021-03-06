package com.food.order.model.repository;


import com.food.order.model.entity.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BlacklistRepository extends JpaSpecificationExecutor<Blacklist>,JpaRepository<Blacklist, Long>,PagingAndSortingRepository<Blacklist, Long> {


}
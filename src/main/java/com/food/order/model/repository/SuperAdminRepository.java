package com.food.order.model.repository;


import com.food.order.model.entity.SuperAdmin;
import com.food.order.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 平台超管
 * Created by Administrator on 2018/4/2.
 */

@Repository
public interface SuperAdminRepository extends JpaSpecificationExecutor<SuperAdmin>,JpaRepository<SuperAdmin, Long>,PagingAndSortingRepository<SuperAdmin, Long> {

}
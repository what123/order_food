package com.food.order.model.repository;


import com.food.order.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2018/4/2.
 */

@Repository
public interface UserRepository extends JpaSpecificationExecutor<User>,JpaRepository<User, Long>,PagingAndSortingRepository<User, Long> {
    User findOneByAccountAndBelongAndIsDelete(String account,int belong,boolean isDelete);

}
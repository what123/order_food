package com.food.order.model.repository;


import com.food.order.model.entity.TmpCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2018/4/2.
 */

@Repository
public interface TmpCacheRepository extends JpaSpecificationExecutor<TmpCache>,JpaRepository<TmpCache, Long>,PagingAndSortingRepository<TmpCache, Long> {
        TmpCache findOneByCKey(String cKey);
}
package com.food.order.model.repository;


import com.food.order.model.entity.Goods;
import com.food.order.model.entity.GoodsTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GoodsTagRepository extends JpaSpecificationExecutor<GoodsTag>,JpaRepository<GoodsTag, Long>,PagingAndSortingRepository<GoodsTag, Long> {


}
package com.food.order.model.repository;


import com.food.order.model.entity.Plugins;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PluginsRepository extends JpaSpecificationExecutor<Plugins>,JpaRepository<Plugins, Long>,PagingAndSortingRepository<Plugins, Long> {


}
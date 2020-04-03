package com.food.order.model.repository;


import com.food.order.model.entity.Goods;
import com.food.order.model.entity.Printer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PrinterRepository extends JpaSpecificationExecutor<Printer>,JpaRepository<Printer, Long>,PagingAndSortingRepository<Printer, Long> {


}
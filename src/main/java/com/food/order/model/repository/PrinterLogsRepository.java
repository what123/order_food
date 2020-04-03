package com.food.order.model.repository;


import com.food.order.model.entity.Printer;
import com.food.order.model.entity.PrinterLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PrinterLogsRepository extends JpaSpecificationExecutor<PrinterLogs>,JpaRepository<PrinterLogs, Long>,PagingAndSortingRepository<PrinterLogs, Long> {


}
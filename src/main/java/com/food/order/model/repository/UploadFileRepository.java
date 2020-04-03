package com.food.order.model.repository;


import com.food.order.model.entity.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2018/4/2.
 */

@Repository
public interface UploadFileRepository extends JpaSpecificationExecutor<UploadFile>,JpaRepository<UploadFile, Long>,PagingAndSortingRepository<UploadFile, Long> {
    UploadFile findOneByMd5(String md5);
}
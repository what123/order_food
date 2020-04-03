package com.food.order.model.repository;


import com.food.order.model.entity.OauthUser;
import com.food.order.model.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OauthUserRepository extends JpaSpecificationExecutor<OauthUser>,JpaRepository<OauthUser, Long>,PagingAndSortingRepository<OauthUser, Long> {
    OauthUser findOneByOpenidAndTagAndType(String openid,String tag,int type);

}
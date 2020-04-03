package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 生成的节课
 * Created by Administrator on 2018/4/2.
 */
@Data
@Entity
public class UploadFile extends BaseEntity{

    private String name;
    @Column(columnDefinition = "text")
    private String des = null;//简介描述
    private String ext;
    private String type = "img";
    private String url;
    private String md5;
    private int pointCount = 1;//引用,等于1时删除才能真正删除
    private String thumbnail;//缩略图
    private Long size;
}

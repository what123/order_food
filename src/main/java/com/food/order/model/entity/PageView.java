package com.food.order.model.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Map;

/**
 * 点餐主题页面
 */
@Data
@Entity
public class PageView extends BaseEntity {
    private String name;//显示名称
    private String pluginPath;
    private String picPath = "preview.png";//图片



}

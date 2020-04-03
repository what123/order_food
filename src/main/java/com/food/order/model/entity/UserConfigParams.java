package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * 设置参数
 */
@Data
public class UserConfigParams {
    private String key;
    private String name;
    private String value;
}

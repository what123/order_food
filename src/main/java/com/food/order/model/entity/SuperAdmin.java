package com.food.order.model.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.List;

/**
 * 超管
 */
@Data
@Entity
public class SuperAdmin extends BaseEntity {
    private String name;//店员名
    private Integer sex;
    private Integer age;
    private String telephon;//电话
    @OneToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private User user;//超管

}

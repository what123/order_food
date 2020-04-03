package com.food.order.plugins.pay;

import java.lang.annotation.*;

/**
 * 支付配置的注解，用于扫描支付类
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PayTarget {
}

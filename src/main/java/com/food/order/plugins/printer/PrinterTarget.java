package com.food.order.plugins.printer;

import java.lang.annotation.*;

/**
 * 打印机配置的注解，用于扫描打印机类
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PrinterTarget {
}

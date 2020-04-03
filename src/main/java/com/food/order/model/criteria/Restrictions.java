package com.food.order.model.criteria;

import com.food.order.model.criteria.Criterion.MatchMode;
import com.food.order.model.criteria.Criterion.Operator;


import java.util.Collection;


/**
 * 条件构造器
 * 用于创建条件表达式
 */
public class Restrictions {

    /**
     * 不为空
     * @param fieldName : 匹配字段
     * @return
     */
    public static SimpleExpression isNotEmpty(String fieldName) {
        return new SimpleExpression(fieldName, Operator.ISNOTEMPTY);
    }

    /**
     * 为空
     * @param fieldName : 匹配字段
     * @return
     */
    public static SimpleExpression isEmpty(String fieldName) {
        return new SimpleExpression(fieldName, Operator.ISEMPTY);
    }

    /**
     * 为空
     * @param fieldName : 匹配字段
     * @return
     */
    public static SimpleExpression isNull(String fieldName) {
        return new SimpleExpression(fieldName, Operator.ISNULL);
    }

    /**
     * 不为空
     * @param fieldName : 匹配字段
     * @return
     */
    public static SimpleExpression isNotNull(String fieldName) {
        return new SimpleExpression(fieldName, Operator.ISNOTNULL);
    }

    /**
     * 等于
     * @param fieldName : 匹配字段
     * @param value : 匹配值
     * @return
     */
    public static SimpleExpression eq(String fieldName, Object value) {
        if(value.equals(null))return null;
        return new SimpleExpression(fieldName, value, Operator.EQ);
    }

    /**
     * 等于 （函数条件查询）
     * @param projection : Projection查询条件(Projections.MAX\SUM\AVG...)
     * @param value : 匹配值
     * @return
     */
    public static ProjectionExpression eq(Projection projection, Object value) {
        if(value.equals(null))return null;
        return new ProjectionExpression(projection.getCol(), value, projection.getType(), Operator.EQ);
    }

    /**
     * 不等于
     * @param fieldName : 匹配字段
     * @param value : 匹配值
     * @return
     */
    public static SimpleExpression ne(String fieldName, Object value) {
        if(value.equals(null))return null;
        return new SimpleExpression(fieldName, value, Operator.NE);
    }

    /**
     * 不等于（函数条件查询）
     * @param projection : Projection查询条件(Projections.MAX\SUM\AVG...)
     * @param value : 匹配值
     * @return
     */
    public static ProjectionExpression ne(Projection projection, Object value) {
        if(value.equals(null))return null;
        return new ProjectionExpression(projection.getCol(), value, projection.getType(), Operator.NE);
    }

    /**
     * 模糊匹配
     * @param fieldName : 匹配字段
     * @param value : 匹配值
     * @return
     */
    public static SimpleExpression like(String fieldName, String value) {
        if(value.equals(null))return null;
        return new SimpleExpression(fieldName, value, Operator.LIKE);
    }

    /**
     * 模糊匹配 （函数条件查询）
     * @param projection : Projection查询条件(Projections.MAX\SUM\AVG...)
     * @param value : 匹配值
     * @return
     */
    public static ProjectionExpression like(Projection projection, String value) {
        if(value.equals(null))return null;
        return new ProjectionExpression(projection.getCol(), value, projection.getType(), Operator.LIKE);
    }

    /**
     *  自定义模式模糊匹配
     * @param fieldName : 匹配字段
     * @param value : 匹配值
     * @param matchMode : 匹配方式(MatchMode.START\END\ANYWHERE)
     * @return
     */
    public static SimpleExpression like(String fieldName, String value,
                                        MatchMode matchMode) {
        if(value.equals(null))return null;
        return new SimpleExpression(fieldName, value, matchMode, Operator.LIKE);
    }

    /**
     *  自定义模式模糊匹配（函数条件查询）
     * @param projection : Projection查询条件(Projections.MAX\SUM\AVG...)
     * @param value  : 匹配值
     * @param matchMode : 匹配方式(MatchMode.START\END\ANYWHERE)
     * @return
     */
    public static ProjectionExpression like(Projection projection, String value,
                                            MatchMode matchMode) {
        if(value.equals(null))return null;
        return new ProjectionExpression(projection.getCol(), value, projection.getType(), Operator.LIKE, matchMode);
    }

    /**
     * 大于
     * @param fieldName : 匹配字段
     * @param value : 匹配值
     * @return
     */
    public static SimpleExpression gt(String fieldName, Object value) {
        if(value.equals(null))return null;
        return new SimpleExpression(fieldName, value, Operator.GT);
    }

    /**
     * 大于（函数条件查询）
     * @param projection : Projection查询条件(Projections.MAX\SUM\AVG...)
     * @param value : 匹配值
     * @return
     */
    public static ProjectionExpression gt(Projection projection, Object value) {
        if(value.equals(null))return null;
        return new ProjectionExpression(projection.getCol(), value, projection.getType(), Operator.GT);
    }

    /**
     * 小于
     * @param fieldName : 匹配字段
     * @param value : 匹配值
     * @return
     */
    public static SimpleExpression lt(String fieldName, Object value) {
        if(value.equals(null))return null;
        return new SimpleExpression(fieldName, value, Operator.LT);
    }

    /**
     * 小于（函数条件查询）
     * @param projection : Projection查询条件(Projections.MAX\SUM\AVG...)
     * @param value : 匹配值
     * @return
     */
    public static ProjectionExpression lt(Projection projection, Object value) {
        if(value.equals(null))return null;
        return new ProjectionExpression(projection.getCol(), value, projection.getType(), Operator.LT);
    }

    /**
     * 小于等于
     * @param fieldName : 匹配字段
     * @param value : 匹配值
     * @return
     */
    public static SimpleExpression lte(String fieldName, Object value) {
        if(value.equals(null))return null;
        return new SimpleExpression(fieldName, value, Operator.LTE);
    }

    /**
     * 小于等于（函数条件查询）
     * @param projection : Projection查询条件(Projections.MAX\SUM\AVG...)
     * @param value : 匹配值
     * @return
     */
    public static ProjectionExpression lte(Projection projection, Object value) {
        if(value.equals(null))return null;
        return new ProjectionExpression(projection.getCol(), value, projection.getType(), Operator.LTE);
    }

    /**
     * 大于等于
     * @param fieldName : 匹配字段
     * @param value : 匹配值
     * @return
     */
    public static SimpleExpression gte(String fieldName, Object value) {
        if(value.equals(null))return null;
        return new SimpleExpression(fieldName, value, Operator.GTE);
    }

    /**
     * 大于等于
     * @param projection : Projection查询条件(Projections.MAX\SUM\AVG...)
     * @param value : 匹配值
     * @return
     */
    public static ProjectionExpression gte(Projection projection, Object value) {
        if(value.equals(null))return null;
        return new ProjectionExpression(projection.getCol(), value, projection.getType(), Operator.GTE);
    }


    /**
     * 或者
     * @param criterions
     * @return
     */
    public static LogicalExpression or(Criterion... criterions){
        return new LogicalExpression(criterions, Operator.OR);
    }


    /**
     * 区间
     * @param column : 匹配字段
     * @param1 val1 左区间
     * @param2 val2 右区间
     * @return
     */
    public static LogicalExpression between(String column, Object val1, Object val2){
        return new LogicalExpression(column, val1, val2, Operator.BETWEEN);
    }




    /**
     * 包含于
     * @param fieldName : 匹配字段
     * @param value : 匹配值
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static LogicalExpression in(String fieldName, Collection value) {
        SimpleExpression[] ses = new SimpleExpression[value.size()];
        int i=0;
        for(Object obj : value){
            ses[i]=new SimpleExpression(fieldName,obj,Operator.EQ);
            i++;
        }
        return new LogicalExpression(ses,Operator.OR);
    }

    /**
     * 包含于
     * @param fieldName : 匹配字段
     * @param value : 匹配值
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static LogicalExpression notIn(String fieldName, Collection value) {
        SimpleExpression[] ses = new SimpleExpression[value.size()];
        int i=0;
        for(Object obj : value){
            ses[i]=new SimpleExpression(fieldName,obj,Operator.NE);
            i++;
        }
        return new LogicalExpression(ses,Operator.AND);
    }
    /**
     * 集合包含某几个元素，譬如可以查询User类中Set<String> set包含"ABC","bcd"的User集合，
     * 或者查询User中Set<Address>的Address的name为"北京"的所有User集合
     * 集合可以为基本类型或者JavaBean，可以是one to many或者是@ElementCollection
     * @param fieldName
     * 列名
     * @param value
     * 集合
     * @return
     * expresssion
     */
//    public static LogicalExpression hasMembers(String fieldName, Object... value) {
//        SimpleExpression[] ses = new SimpleExpression[value.length];
//        int i = 0;
//        //集合中对象是基本类型，如Set<Long>，List<String>
//        Criterion.Operator operator = Criterion.Operator.IS_MEMBER;
//        //集合中对象是JavaBean
//        if (fieldName.contains(".")) {
//            operator = Criterion.Operator.EQ;
//        }
//        for (Object obj : value) {
//            ses[i] = new SimpleExpression(fieldName, obj, operator);
//            i++;
//        }
//        return new LogicalExpression(ses, Criterion.Operator.OR);
//    }

}
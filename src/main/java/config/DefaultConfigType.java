package main.java.config;

/**
 * 默认配置类名
 * 主要用于自动扫描时 读取相应的类
 */
public enum DefaultConfigType {
    DateEntity, //实体父类
    SingleParam,//查询单参数
    ResultVo,//控制器公共返回
    IVo,//查询接口
    QueryProvide,//查询注解
    QueryField,//查询字段
    JpaWrapper,//jpa封装
    CommonUtils,//copyProperty工具类

    SqlExpression,//Sql表达式
    QueryPresentCondition,//查询当前条件

    DictDescribe,//字典描述注解

    ValidateType,//验证类型枚举
    Effective,//验证有效注解
    Unique,//验证唯一注解

    ;
}

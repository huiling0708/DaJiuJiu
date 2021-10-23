package main.java.common;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表格中的字段
 * 实现ITableBean的类字段上标记 用于生成JTable中的列数据和行数据
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ITableField {

    int sort();//顺序

    String columnName();//表列名

    int preferredWidth() default 150;//列宽

    boolean notCellEditable() default false;//不允许编辑
}

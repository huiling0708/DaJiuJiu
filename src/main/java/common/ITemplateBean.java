package main.java.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模版bean
 * 配置在bean的class上，便于生成Freemarker 生成模版类容
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ITemplateBean {
    //模版名称
    String value();
}

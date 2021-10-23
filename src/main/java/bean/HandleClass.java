package main.java.bean;

import lombok.Getter;
import lombok.Setter;
import main.java.common.IHandleClass;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 处理类bean
 * 主要用于把实体类的PsiClass转换为具体的Java类时的处理帮助类
 * 实体类转换为 vo查询视图类、param参数类、service服务类
 */
@Getter
@Setter
public class HandleClass implements IHandleClass {

    private String className;//类名
    private String description;//描述
    private String queryName;//查询方法名称 仅在转换为Vo类时使用
    private String entityClassName;//实体类名
    private String entityClassNameParam;//实体类名参数
    private String serialVersionUIDValue;//序列号版本号

    private List<HandleClassField> fields = new ArrayList<>();//字段
    private Set<String> importContent = new LinkedHashSet<>();//导入

}

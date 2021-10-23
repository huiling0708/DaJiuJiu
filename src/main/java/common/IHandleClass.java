package main.java.common;


import java.util.Set;

/**
 * 处理类接口
 * 主要用于 实体类生成其它类时使用相关字段
 */
public interface IHandleClass extends IJavaFileClass {

    String getClassName();

    void setClassName(String className);

    String getDescription();

    void setDescription(String description);

    Set<String> getImportContent();

    void setImportContent(Set<String> importContent);

    @Override
    default String javaClassName() {
        return this.getClassName();
    }
}

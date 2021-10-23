package main.java.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 方法处理类型
 */
@Getter
@AllArgsConstructor
public enum MethodHandleType {
    SAVE("保存", "addServiceMethodSave.ftl"),
    UPDATE("更新", "addServiceMethodUpdate.ftl"),
    DELETE("删除", "addServiceMethodDelete.ftl"),
    ;
    private String description;
    private String templateName;

    public String getMethodName(String entityClassName) {
        return this.name().toLowerCase() + entityClassName;
    }

    public String getMethodDescription(String entityDescription) {
        return this.description + entityDescription;
    }
}

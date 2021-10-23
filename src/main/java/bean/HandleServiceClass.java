package main.java.bean;


import lombok.Getter;
import lombok.Setter;
import main.java.common.IHandleClass;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 处理服务bean
 */
@Getter
@Setter
public class HandleServiceClass implements IHandleClass {

    private String className;//类名
    private String description;//描述

    private Set<String> importContent = new LinkedHashSet<>();//导入

}

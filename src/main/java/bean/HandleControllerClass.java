package main.java.bean;

import lombok.Getter;
import lombok.Setter;
import main.java.common.DJJHelper;
import main.java.common.IJavaFileClass;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * 主要用于把服务类的PsiClass转换为具体的Controller类时的处理帮助类
 */
@Getter
@Setter
public class HandleControllerClass implements IJavaFileClass {

    private String classBeanName;//类元名称 去掉service后的名称
    private String serviceName;//服务类名
    private String controllerName;//控制器类名
    private String description;//描述
    private String mappingValue;//控制器mapping值
    private String serviceParam;//服务类参数时的名称

    private List<HandleControllerMethod> methods = new ArrayList<>();//方法
    private Set<String> importContent = new LinkedHashSet<>();//导入

    public void setClassBeanName(String classBeanName) {
        this.classBeanName = classBeanName;
        this.serviceName = classBeanName + "Service";
        this.controllerName = classBeanName + "Controller";
        this.mappingValue = DJJHelper.buildMappingValue(classBeanName);
        this.serviceParam = DJJHelper.firstLowerCase(this.serviceName);
    }

    @Override
    public String javaClassName() {
        return controllerName;
    }
}

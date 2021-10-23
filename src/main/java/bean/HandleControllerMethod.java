package main.java.bean;

import lombok.Getter;
import lombok.Setter;
import main.java.common.ITableBean;
import main.java.common.ITableField;

/**
 * 主要用于把服务类的中的PsiMethod方法提取出来，用于生成控制器的方法
 */
@Getter
@Setter
public class HandleControllerMethod implements ITableBean {

    private String name;//名称
    @ITableField(sort = 1, columnName = "Describe")
    private String description;//描述
    @ITableField(sort = 3, columnName = "ParamType", notCellEditable = true)
    private String paramType;//参数类型
    @ITableField(sort = 4, columnName = "ReturnType", notCellEditable = true)
    private String returnType;//返回类型
    @ITableField(sort = 2, columnName = "MappingValue")
    private String mappingValue;//方法在控制器中的mapping值
    private boolean singleParam;
    private boolean selected = true;//选中

    private String serviceParam;//仅在单个方法转换时使用

    private String serviceMethodName;//service 中的方法名称
}

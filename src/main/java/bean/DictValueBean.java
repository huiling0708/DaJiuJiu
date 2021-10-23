package main.java.bean;

import lombok.Getter;
import lombok.Setter;
import main.java.common.ITableBean;
import main.java.common.ITableField;

/**
 * 字典枚举值bean
 */
@Getter
@Setter
public class DictValueBean implements ITableBean {

    @ITableField(sort = 1, columnName = "Index", preferredWidth = 10,notCellEditable = true)
    private String index;
    @ITableField(sort = 2, columnName = "Value")
    private String name;//名称
    @ITableField(sort = 3, columnName = "Describe")
    private String describe;//描述
    private boolean selected = false;//选中

    public DictValueBean(String index) {
        this.index = index;
        this.name = "";
        this.describe = "";
    }
}

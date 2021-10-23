package main.java.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.java.common.DJJHelper;
import main.java.common.IJavaFileClass;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据主键实体
 */
@Getter
@Setter
@NoArgsConstructor
public class DataPkEntity implements IJavaFileClass {

    private String serialVersionUIDValue;//序列号版本号
    private String pkClassName;//复合主键类名称
    //字段
    private List<DataField> dataFields = new ArrayList<>();

    public DataPkEntity(String pkClassName, List<DataField> dataFields) {
        this.serialVersionUIDValue = DJJHelper.buildSerialVersionUID();
        this.pkClassName = pkClassName;
        this.dataFields = dataFields;
    }

    @Override
    public String javaClassName() {
        return this.pkClassName;
    }
}
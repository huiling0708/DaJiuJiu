package main.java.bean;

import lombok.Data;
import main.java.common.*;

import java.io.Serializable;

/**
 * 数据字段
 */
@Data
public class DataField implements Serializable, ITableBean {

    private static final long serialVersionUID = 4374941399029466482L;
    @ITableField(sort = 2, columnName = "Describe")
    private String describe;//描述
    private String column;//列
    private String dataType = "Undefined";//数据类型
    private Integer length;//长度
    private Integer precision;//精度
    private Integer scale;//小数
    private boolean primary;//是否是主键
    private boolean mandatory;//是否不能为空
    @ITableField(sort = 1, columnName = "Name")
    private String fieldName;// 字段名称
    @ITableField(sort = 3, columnName = "Type")
    private String javaType = "Undefined";//java 类型
    //附加
    private boolean unique = false;//唯一索引
    private boolean insertable = true;//允许插入
    private boolean updatable = true;//允许更新
    private String columnDefinition = null;//自定义字段格式
    private String dictQualifiedName;//数据字典限定名称

    private boolean selected = true;//选中

    @ITableField(sort = 4, columnName = "@Column", preferredWidth = 200)
    private String jpaColumnContent;//jpa 注解内容

    public void buildJpaColumnContent() {
        this.jpaColumnContent = new JpaColumnBuilder(this).toString();
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;//数据类型
        TypeMapping typeMapping = TypeMapping.getTypeMappingByDataType(dataType);
        this.setJavaType(typeMapping.getJavaType(this.isMandatory())); //java 类型
    }

    /**
     * 导入处理
     *
     * @param dataEntity
     */
    public void importHandle(DataEntity dataEntity) {
        String dictQualifiedName = this.getDictQualifiedName();
        if (!DJJHelper.isBlank(dictQualifiedName)) {
            dataEntity.getImportContent().add(dictQualifiedName);
            dataEntity.setExistDict(true);
            return;
        }
        TypeMapping typeMapping = TypeMapping.getTypeMappingByJavaType(javaType);
        if (typeMapping == null) {
            return;
        }
        String packageName = typeMapping.getPackageName();
        if (!DJJHelper.isBlank(packageName)) {
            dataEntity.getImportContent().add(packageName);
        }
    }

    /**
     * 字段长度 用于展示
     *
     * @return
     */
    public String getLengthShow() {
        if (precision != null && scale != null) {
            return precision + "," + scale;
        } else if (length != null) {
            return length + "";
        }
        return null;
    }

}

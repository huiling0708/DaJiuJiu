package main.java.common;


import main.java.bean.DataField;

/**
 * Jpa 列注解内容生成器
 */
public class JpaColumnBuilder {
    //@Column(
    // name = "", unique = false, insertable = true,
    // updatable = true, columnDefinition = "",
    //nullable = true,length = 255, precision = 0, scale = 0)
    public JpaColumnBuilder(DataField dataField) {
        //无精度时再判断长度
        if (dataField.getPrecision() != null) {
            this.addCondition("precision = ");
            this.add(dataField.getPrecision());
            if (dataField.getScale() != null) {
                this.addCondition("scale = ");
                this.add(dataField.getScale());
            }
        } else {
            if (dataField.getLength() != null && dataField.getLength() != 255) {
                this.addCondition("length = ");
                this.add(dataField.getLength());
            }
        }
        //是否不能为空
        // 去掉基础类型判断 && !TypeMapping.basicJavaType(dataField.getJavaType())
        if (dataField.isMandatory() && !dataField.isPrimary()) {
            this.addCondition("nullable = false");
        }
        //唯一索引
        if (dataField.isUnique()) {
            this.addCondition("unique = true");
        }
        //允许插入与允许更新
        if (!dataField.isInsertable()) {
            this.addCondition("insertable = false");
        }
        if (!dataField.isUpdatable()) {
            this.addCondition("updatable = false");
        }
        if (dataField.getColumnDefinition() != null && dataField.getColumnDefinition().trim() != "") {
            this.addCondition("columnDefinition = ");
            this.add(dataField.getColumnDefinition().trim());
        }
    }

    private StringBuilder builder;

    //添加条件
    private void addCondition(String value) {
        if (builder == null) {
            this.builder = new StringBuilder("(");
        } else {
            this.builder.append(",");
        }
        this.builder.append(value);
    }

    //添加值
    private void add(Object value) {
        this.builder.append(value);
    }

    @Override
    public String toString() {
        if (this.builder == null) {
            return null;
        }
        this.builder.append(")");
        return this.builder.toString();
    }
}

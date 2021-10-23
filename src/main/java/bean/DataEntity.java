package main.java.bean;

import lombok.Data;
import main.java.common.IJavaFileClass;
import main.java.common.ITableBean;
import main.java.common.ITableField;
import main.java.common.TypeMapping;

import java.util.*;

/**
 * 数据实体
 * 通过数据源生成
 */
@Data
public class DataEntity implements IJavaFileClass, ITableBean {

    @ITableField(sort = 1, columnName = "Name",notCellEditable = true)
    private String tableName = "Undefined";//表名
    private String className = "Undefined";//类名
    @ITableField(sort = 2, columnName = "Describe",notCellEditable = true)
    private String describe;//描述
    private int primaryCount = 0;//主键个数
    private String primaryJavaType;//主键java 类型
    private String primaryStrategy;//主键生成策略
    private String serialVersionUIDValue;//序列号版本号

    private String pkClassName;//复合主键类名称
    private boolean existDict;//存在数据字典
    private boolean selected = true;//选中

    private List<DataField> dataFields = new ArrayList<>();//字段
    private Set<String> importContent = new LinkedHashSet<>();//导入


    private final static Set<String> PASS_FIELD;//跳过的字段 可以配置在配置文件中

    static {
        PASS_FIELD = new HashSet<>(Arrays.asList(
                "createUser", "updateUser", "createTime", "updateTime"
        ));
    }

    /**
     * 添加字段
     *
     * @param dataField
     */
    public void addField(DataField dataField) {
        if (dataField == null) {
            return;
        }
        if (!PASS_FIELD.contains(dataField.getFieldName())) {
            dataField.buildJpaColumnContent();
            this.getDataFields().add(dataField);
        }
    }

    /**
     * 添加主键个数 并设置主键java 类型 ，多主键时暂时覆盖
     *
     * @param primaryJavaType
     */
    public void addPrimaryCount(String primaryJavaType) {
        this.primaryCount++;
        this.primaryJavaType = TypeMapping.getPackingTypeByJavaType(primaryJavaType);
    }

    /**
     * 初始化主键策略
     *
     * @return
     */
    public void initPrimaryStrategy() {
        if (this.primaryCount == 1) {
            this.primaryStrategy = TypeMapping.primaryStrategy(this);
        } else {
            this.primaryStrategy = null;
        }
    }

    @Override
    public String javaClassName() {
        return this.className;
    }
}
package main.java.bean;

import lombok.Getter;
import lombok.Setter;
import main.java.common.DJJHelper;
import main.java.common.MethodHandleType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 创建服务方法bean
 */
@Getter
@Setter
public class HandleServiceMethod {
    private String methodName;//方法名
    private String description;//描述
    private MethodHandleType handleType;//方法处理类型
    private String relationEntity;//关联实体
    private String relationEntityDescription;//关联实体秒速
    private String inputParam;//入参
    private String outputParam;//出参
    private boolean checkExists;
    private String entityParam;//实体参数，生成方法时在模版中使用

    private List<HandleNonEditableField> updateFields = new ArrayList<>();//更新字段
    private List<HandleNonEditableField> conditionFields = new ArrayList<>();//条件字段
    private List<HandleNonEditableField> saveNeedSetValueFields;//保存时需要额外set的字段 即参数类中没有的字段

    private Set<String> importContent = new LinkedHashSet<>();//导入

    public void addImport(String value) {
        if (DJJHelper.isBlank(value)
                || ("void".equals(value)
                || "int".equals(value))) {
            return;
        }
        this.importContent.add(value);
    }

    public void setRelationEntity(String relationEntity) {
        this.relationEntity = relationEntity;
        String simple = this.getRelationEntitySimple();
        if (simple == null || simple.length() > 15) {
            simple = "entity";
        } else {
            simple = DJJHelper.firstLowerCase(simple);
        }
        this.entityParam = simple;
    }

    public String getRelationEntitySimple() {
        return DJJHelper.getSimpleName(this.relationEntity);
    }

    public String getInputParamSimple() {
        if (DJJHelper.isBlank(this.inputParam)) {
            return null;
        }
        return DJJHelper.getSimpleName(this.inputParam);
    }

    public String getOutputParamSimple() {
        if (DJJHelper.isBlank(this.outputParam)) {
            return null;
        }
        return DJJHelper.getSimpleName(this.outputParam);
    }

}

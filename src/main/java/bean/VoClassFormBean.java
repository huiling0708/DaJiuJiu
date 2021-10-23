package main.java.bean;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.java.common.PsiHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Vo窗体bean
 *  用于显示 ChekEntityFieldForm 窗体中的类容
 */
@Getter
@Setter
@NoArgsConstructor
public class VoClassFormBean {

    private String entityName;//实体名称
    private String entityDescription;//实体描述
    private String fieldName;//字段名称
    private String fieldDescription;//字段描述
    private PsiClass entityClass;
    private PsiField psiField;

    private List<VoClassBean> otherVoClasses = new ArrayList<>();
    private List<VoClassBean> needAddFieldVoClasses = new ArrayList<>();

    public String getEntityText() {
        return this.entityName + "  " + entityDescription;
    }

    public String getFieldText() {
        return this.fieldName + "  " + fieldDescription;
    }

    public VoClassFormBean(PsiClass entityClass, PsiField psiField, List<VoClassBean> otherVoClasses, List<VoClassBean> needAddFieldVoClasses) {
        this.entityClass = entityClass;
        this.psiField = psiField;
        this.otherVoClasses = otherVoClasses;
        this.needAddFieldVoClasses = needAddFieldVoClasses;
        this.entityName = entityClass.getName();
        this.entityDescription = PsiHelper.getEntityDescription(entityClass);
        this.fieldName = psiField.getName();
        this.fieldDescription = PsiHelper.getEntityFieldDescription(psiField);
    }

}

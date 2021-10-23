package main.java.bean;

import com.intellij.psi.PsiClass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.java.common.DJJHelper;

@Getter
@Setter
@NoArgsConstructor
public class ValidateTypeBean {

    private String typeName;
    private String typeDescribe;
    private String entityName;
    private String fieldName;
    private PsiClass entityClass;

    public ValidateTypeBean(String typeName, String typeDescribe, String fieldName, PsiClass entityClass) {
        this.typeName = typeName;
        this.typeDescribe = typeDescribe;
        this.entityClass = entityClass;
        this.entityName = entityClass.getName();
        this.fieldName = fieldName;
    }

    public String getFieldGetMethodName() {
        return "get" + DJJHelper.firstToUpperCase(this.fieldName);
    }
}

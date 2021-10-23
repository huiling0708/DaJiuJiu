package main.java.bean;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import lombok.Getter;
import lombok.Setter;
import main.java.common.DJJHelper;
import main.java.common.ITableBean;
import main.java.common.ITableField;
import main.java.common.PsiHelper;

import java.beans.Customizer;
import java.util.ArrayList;
import java.util.List;

/**
 * 不可编辑字段通用bean
 */
@Getter
@Setter
public class HandleNonEditableField implements ITableBean {

    @ITableField(sort = 1, columnName = "Name", preferredWidth = 100, notCellEditable = true)
    private String name;//名称
    @ITableField(sort = 2, columnName = "Describe", preferredWidth = 100, notCellEditable = true)
    private String description;//描述
    @ITableField(sort = 3, columnName = "Type", preferredWidth = 60, notCellEditable = true)
    private String type;//类型

    private PsiType psiType;//psi数据类型
    private boolean selected;//选中
    private boolean existsParam;//存在于参数 在参数类中是否能够被找到

    public String getNameParam() {
        return DJJHelper.firstToUpperCase(name);
    }

    public HandleNonEditableField(PsiField psiField) {
        this.name = psiField.getName();
        this.description = PsiHelper.getEntityFieldDescription(psiField);
        this.type = psiField.getType().getPresentableText();
        this.psiType = psiField.getType();
        this.selected = false;
    }

    /**
     * 生成list
     * @param psiClass
     * @return
     */
    public static List<HandleNonEditableField> buildFieldList(PsiClass psiClass) {
        List<HandleNonEditableField> fieldList = new ArrayList<>();
        for (PsiField field : psiClass.getFields()) {
            HandleNonEditableField methodField = new HandleNonEditableField(field);
            if (DJJHelper.isBlank(methodField.getDescription())) {
                continue;
            }
            fieldList.add(methodField);
        }
        return fieldList;
    }
}

package main.java.bean;


import com.intellij.psi.PsiClass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.java.common.ITableBean;
import main.java.common.ITableField;
import main.java.common.PsiHelper;

/**
 * Vo转换bean
 */
@Getter
@Setter
@NoArgsConstructor
public class VoClassBean  implements ITableBean {

    @ITableField(sort = 1, columnName = "Name",notCellEditable = true)
    private String className;//类名
    @ITableField(sort = 2, columnName = "Describe",notCellEditable = true)
    private String description;//描述
    private boolean selected;//选中
    private PsiClass psiClass;

    public VoClassBean(PsiClass psiClass) {
        this.psiClass=psiClass;
        this.className = psiClass.getName();
        this.description = PsiHelper.getApiModelDescription(psiClass);
        this.selected = true;
    }
}

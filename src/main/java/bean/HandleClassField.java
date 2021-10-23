package main.java.bean;

import com.intellij.psi.PsiType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.java.common.DJJHelper;
import main.java.common.ITableBean;
import main.java.common.ITableField;
import main.java.common.UseNeedImportClass;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理类中的字段详情bean
 */
@Getter
@Setter
@NoArgsConstructor
public class HandleClassField implements ITableBean {

    @ITableField(sort = 1, columnName = "Name")
    private String name;//名称
    @ITableField(sort = 2, columnName = "Describe", preferredWidth = 130)
    private String description;//描述
    @ITableField(sort = 3, columnName = "Type", preferredWidth = 80)
    private String type;//类型
    @ITableField(sort = 4, columnName = "Annotation", preferredWidth = 80)
    private String annotationContent;//注解类容
    private boolean selected = true;//选中
    private boolean notNull;//不能为空 参数时使用

    private PsiType psiType;//psi类型

    private QueryFieldBean queryFieldBean;//查询字段设置
    private List<ValidateAnnotationBean> annotationBeanList;//参数验证注解
    private JFrame paramFrame;

    public HandleClassField(String name, String description, PsiType psiType) {
        this.name = name;
        this.description = description;
        this.type = psiType.getPresentableText();
        this.psiType = psiType;
    }

    public void setAnnotationContentAndBean(String annotationContent) {
        this.annotationContent = annotationContent;
        this.annotationBeanList = new ArrayList<>();
        this.annotationBeanList.add(new ValidateAnnotationBean(
                annotationContent, "@" + annotationContent));
    }

    public void setAnnotationBeanList(List<ValidateAnnotationBean> annotationBeanList) {
        this.annotationBeanList = annotationBeanList;
        if (annotationBeanList == null || annotationBeanList.size() == 0) {
            this.annotationContent = null;
            this.notNull = false;
            return;
        }
        for (ValidateAnnotationBean bean : annotationBeanList) {
            String annName = bean.getName();
            if (!notNull) {
                if (UseNeedImportClass.existNotNullAnnotation(annName)) {
                    this.notNull = true;
                }
            }
        }
        String content = annotationBeanList.get(0).getName();
        if (annotationBeanList.size() > 1) {
            content += "...";
        }
        this.annotationContent = content;
    }

    public boolean isExistAnn() {
        return !DJJHelper.isBlank(this.annotationContent);
    }

    public String getQueryFieldContent() {
        if (!isExistAnn()) {
            return null;
        }
        QueryFieldBean bean = this.getQueryFieldBean();
        if (bean == null) {
            return "@" + annotationContent;
        }
        return bean.buildQueryFieldContent();
    }
}

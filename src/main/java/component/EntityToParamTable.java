package main.java.component;


import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import main.java.bean.HandleClassField;
import main.java.common.TableDataHandle;
import main.java.common.TypeMapping;
import main.java.common.UseNeedImportClass;
import main.java.form.ParamSetUpForm;

import javax.swing.*;

/**
 * 实体类转换为Param参数类表格
 */
public class EntityToParamTable extends BaseDJJTable<HandleClassField> {

    public EntityToParamTable(TableDataHandle<HandleClassField> handle) {
        super(handle);
    }

    private PsiClass entityClass;
    private Project project;

    public EntityToParamTable setProjectAndEntityClass(Project project, PsiClass entityClass) {
        this.entityClass = entityClass;
        this.project = project;
        return this;
    }

    @Override
    protected void init() {
        super.init();
        //第4列为Java类型下拉
        this.getColumnModel().getColumn(3).setCellEditor(
                new DefaultCellEditor(TypeMapping.getTypeComboBox()));
        //第5列为验证相关注解 设置不允许编辑
        this.getHandle().getNotEditableCells().add(4);

    }

    @Override
    protected void fieldButtonHandle(TableButton button) {
        //点击最后一列的button 打开参数配置页面
        HandleClassField handleClassField = handle.getDataList().get(button.getRow());
        new ParamSetUpForm(project, entityClass, handleClassField, beanList -> {
            handleClassField.setAnnotationBeanList(beanList);
            this.setValueAt(handleClassField.getType(), button.getRow(), 3);
            this.setValueAt(handleClassField.getAnnotationContent(),
                    button.getRow(), 4);
        });
    }
}

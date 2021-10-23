package main.java.component;

import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.TreeJavaClassChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import main.java.bean.DataField;
import main.java.common.TableDataHandle;
import main.java.common.TypeMapping;
import main.java.common.UseNeedImportClass;
import main.java.form.DictForm;

import javax.swing.*;
import javax.swing.event.AncestorListener;

/**
 * 数据源转换为实体时使用的表格
 */
public class DataToEntityTable extends BaseDJJTable<DataField> {

    private Project project;
    private JFrame parentJFrame;//父级窗体 最外层窗体

    public DataToEntityTable(TableDataHandle<DataField> handle) {
        super(handle);
    }

    public DataToEntityTable setProject(Project project) {
        this.project = project;
        return this;
    }

    public DataToEntityTable setParentJFrame(JFrame parentJFrame) {
        this.parentJFrame = parentJFrame;
        return this;
    }

    @Override
    protected void init() {
        super.init();
        //第4列为类型下拉
        this.getColumnModel().getColumn(3).setCellEditor(
                new DefaultCellEditor(TypeMapping.getEntityTypeComboBox()));
    }

    @Override
    public void setValueAt(Object rowValue, int row, int column) {
        if (column == 3) {
            if (TypeMapping.DICT_KEYWORD.equals(rowValue)) {
                //重已有字典选择
                BaseDJJForm.frameVisibleHandle(() -> {
                    this.setDictValueByOld(row, column);
                }, this.parentJFrame, handle.getTableFrame());
                return;
            } else if (TypeMapping.ADD_DICT_KEYWORD.equals(rowValue)) {
                //添加一个新的字典
                this.setDictValueByNew(row, column);
                return;
            } else {
                //清除字段上的字典缓存
                setDictQualifiedName(null, row);
            }
        }
        super.setValueAt(rowValue, row, column);
    }

    /**
     * 创建一个新的字典设置到table中 同时设置字典限定名称到相应的字段中
     * @param row
     * @param column
     */
    private void setDictValueByNew(int row, int column) {
        DataField dataField = this.getHandle().getDataList().get(row);
        String fieldName = dataField.getFieldName();
        String describe = dataField.getDescribe();
        //把原字段的 字段值与字段描述作为 字典类型的初始值
        DictForm dictForm = new DictForm(project, fieldName, describe, dictBean -> {
            if (dictBean == null) {
                this.setFieldOriginalDataType(row,column);
            } else {
                this.setDictQualifiedName(dictBean.getQualifiedName(), row);
                super.setValueAt(dictBean.getDictName(), row, column);
            }
        });
    }

    /**
     * 根据已经存在的字典设置到table中 同时设置字典限定名称到相应的字段中
     * @param row
     * @param column
     */
    private void setDictValueByOld(int row, int column) {
        TreeJavaClassChooserDialog selector = new TreeJavaClassChooserDialog("请选择一个数据字典",
                project, GlobalSearchScope.projectScope(project), new ClassFilter() {
            @Override
            public boolean isAccepted(PsiClass psiClass) {
                PsiAnnotation annotation = psiClass.getAnnotation(
                        UseNeedImportClass.DictAnnotation.getImportContent(project));
                return annotation != null;
            }
        }, null);

        selector.show();
        PsiClass psiClass = selector.getSelected();
        selector.dispose();
        if (psiClass == null) {
            this.setFieldOriginalDataType(row,column);
            return;
        }
        this.setDictQualifiedName(psiClass.getQualifiedName(), row);
        super.setValueAt(psiClass.getName(), row, column);
    }

    /**
     * 设置字段字典限定名称
     *
     * @param qualifiedName
     * @param row
     */
    private void setDictQualifiedName(String qualifiedName, int row) {
        DataField dataField = this.getHandle().getDataList().get(row);
        dataField.setDictQualifiedName(qualifiedName);
    }

    /**
     * 设置字段原有java类型
     * @param row
     * @param column
     */
    private void setFieldOriginalDataType(int row,int column){
        DataField dataField = this.getHandle().getDataList().get(row);
        String dataType = dataField.getDataType();
        TypeMapping typeMapping = TypeMapping.getTypeMappingByDataType(dataType);
        String javaType = typeMapping.getJavaType(dataField.isMandatory());
        super.setValueAt(javaType,row,column);
    }
}

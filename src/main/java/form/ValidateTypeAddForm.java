package main.java.form;

import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.TreeJavaClassChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import main.java.bean.HandleClassField;
import main.java.bean.HandleNonEditableField;
import main.java.bean.ValidateTypeBean;
import main.java.common.*;
import main.java.component.BaseDJJForm;
import main.java.component.NonEditableFieldTable;
import main.java.transform.AddValidateTypeHandler;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 添加验证类型窗体
 */
public class ValidateTypeAddForm extends BaseDJJForm {
    private JPanel mainPanel;
    private JTable classFieldsTable;
    private JButton OKButton;
    private JButton cancelButton;
    private JTextField entityText;
    private JButton chooseButton;
    private JTextField fieldText;
    private JTextField typeName;
    private JTextField typeDescribe;

    private TableDataHandle<HandleNonEditableField> fieldsTableHandle;
    private PsiClass chooseEntityClass;

    public ValidateTypeAddForm(Project project) {
        this(project, null, null, null);
    }

    public ValidateTypeAddForm(Project project, PsiClass entityClass, String fieldName, Consumer<ValidateTypeBean> consumer) {
        JFrame jFrame = this.windowCenter("Add Validate Type", this.mainPanel);
        jFrame.getRootPane().setDefaultButton(OKButton);
        if (entityClass == null) {
            chooseButton.setEnabled(true);
        }
        this.updateClassFieldTableHandle(entityClass);
        //选择实体类
        chooseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frameVisibleHandle(() -> {
                    chooseClassHandle(project);
                }, jFrame);
            }
        });
        //选择字段
        if (!DJJHelper.isBlank(fieldName)) {
            int row = -1;
            List<HandleNonEditableField> dataList = this.fieldsTableHandle.getDataList();
            for (HandleNonEditableField field : dataList) {
                if (field.getName().equals(fieldName)) {
                    row = this.fieldsTableHandle.getDataList().indexOf(field);
                    this.setChooseField(field);
                    break;
                }
            }
            if (row != -1) {
                classFieldsTable.setRowSelectionInterval(row, row);
            }
        }

        //表格监听
        classFieldsTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = classFieldsTable.getSelectedRow();
            if (selectedRow != -1) {
                NonEditableFieldTable table = (NonEditableFieldTable) classFieldsTable;
                HandleNonEditableField field = table.getHandle().getDataList().get(selectedRow);
                this.setChooseField(field);
            }
        });

        //ok 处理
        OKButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ValidateTypeBean typeBean = new ValidateTypeBean(
                        DJJHelper.addLineAndUpperCase(typeName.getText()), typeDescribe.getText(),
                        ((DJJJTextField) fieldText).getFieldName(), chooseEntityClass);
                try {
                    AddValidateTypeHandler.addValidateType(project, typeBean);
                    jFrame.dispose();
                    if (consumer != null) {
                        consumer.accept(typeBean);
                    }
                } catch (Exception exception) {
                    DJJMessage.errorDialog(exception);
                }
            }
        });

        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                jFrame.dispose();
            }
        });
    }

    /**
     * 设置选择的字段
     *
     * @param field
     */
    public void setChooseField(HandleNonEditableField field) {
        DJJJTextField djjjTextField = (DJJJTextField) fieldText;
        djjjTextField.setFieldName(field);
        String upperCase = DJJHelper.addLineAndUpperCase(field.getName());
        typeName.setText(upperCase);
        typeDescribe.setText(field.getDescription());
    }

    /**
     * 选择class处理
     *
     * @param project
     */
    private void chooseClassHandle(Project project) {
        TreeJavaClassChooserDialog selector = new TreeJavaClassChooserDialog("请选择一个实体类",
                project, GlobalSearchScope.projectScope(project), new ClassFilter() {
            @Override
            public boolean isAccepted(PsiClass psiClass) {
                return psiClass.getAnnotation(
                        UseNeedImportClass.Entity.getImportContent()) != null;
            }
        }, null);
        selector.show();
        PsiClass psiClass = selector.getSelected();
        selector.dispose();
        if (psiClass == null) {
            return;
        }
        //更新表格
        this.updateClassFieldTableHandle(psiClass);
    }

    /**
     * 更新class表格内容
     *
     * @param psiClass
     */
    private void updateClassFieldTableHandle(PsiClass psiClass) {
        if (psiClass == null) {
            return;
        }
        this.chooseEntityClass = psiClass;
        entityText.setText(psiClass.getName() + " " + PsiHelper.getEntityDescription(psiClass));
        List<HandleNonEditableField> list =
                HandleNonEditableField.buildFieldList(psiClass);
        this.fieldsTableHandle.setDataList(list);
        NonEditableFieldTable table = (NonEditableFieldTable) this.classFieldsTable;
        table.updateHandle();
    }

    private void createUIComponents() {
        this.fieldsTableHandle = new TableDataHandle<>(
                HandleNonEditableField.class, new ArrayList<>(), false, false);
        this.classFieldsTable = new NonEditableFieldTable(fieldsTableHandle);

        this.fieldText = new DJJJTextField();
    }

    private class DJJJTextField extends JTextField {
        private String fieldName;

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(HandleNonEditableField field) {
            this.fieldName = field.getName();
            this.setText(field.getName() + " " + field.getDescription());
        }
    }
}

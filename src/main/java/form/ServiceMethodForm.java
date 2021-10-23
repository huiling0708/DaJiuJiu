package main.java.form;

import com.intellij.ide.util.TreeJavaClassChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaFile;
import main.java.bean.HandleNonEditableField;
import main.java.bean.HandleServiceMethod;
import main.java.common.*;
import main.java.component.BaseDJJForm;
import main.java.component.NonEditableFieldTable;
import main.java.transform.EntityTransformHandler;
import main.java.transform.ServiceMethodHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Service添加方法窗体
 */
public class ServiceMethodForm extends BaseDJJForm {
    private JTextField methodName;
    private JTextField methodDescribe;
    private JRadioButton saveRadioButton;
    private JRadioButton updateRadioButton;
    private JRadioButton deleteRadioButton;
    private JButton entityChooseButton;
    private JButton cancelButton;
    private JButton OKButton;

    private JTextField relationEntity;
    private JTextField inputParam;
    private JComboBox outputParam;

    private String relationEntityValue;
    private String inputParamValue;
    private String outputParamValue;

    private JButton inputChooseButton;
    private JButton outputChooseButton;
    private JPanel updatePanel;
    private JPanel conditionPanel;
    private JTable updateTable;
    private JTable conditionTable;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JCheckBox checkRecordIsExistsCheckBox;
    private double defaultHeight;

    private PsiClass chooseEntityClass;
    private MethodHandleType methodHandleType;//方法处理类型

    private TableDataHandle<HandleNonEditableField> updateFieldsTableHandle;//更新字段
    private TableDataHandle<HandleNonEditableField> conditionFieldsTableHandle;//条件字段

    /**
     * @param project      项目
     * @param serviceClass 服务类 PsiClass
     * @param entityClass  实体类 PsiClass
     */
    public ServiceMethodForm(Project project, PsiClass serviceClass, PsiClass entityClass) {
        this.titleHandle(serviceClass);
        this.chooseEntityClass = entityClass;
        this.methodHandleType = MethodHandleType.SAVE;
        this.saveRadioButton.setSelected(true);
        this.methodHandle();

        JFrame jFrame = this.windowCenter("Create Method", this.mainPanel);
        jFrame.getRootPane().setDefaultButton(OKButton);
        this.defaultHeight = jFrame.getSize().getHeight();

        //选择方法处理类型监听
        this.otherPanelVisible(jFrame, saveRadioButton, updateRadioButton, deleteRadioButton);

        //选择关联实体类
        this.entityChooseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frameVisibleHandle(() -> {
                    PsiClass psiClass = choosePsiClass(project, serviceClass, "entity");
                    if (psiClass != null) {
                        chooseEntityClass = psiClass;
                        methodHandle();
                    }
                }, jFrame);
            }
        });

        //入参
        this.inputChooseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (chooseEntityClass != null) {
                    int choose = DJJMessage.
                            chooseOptionDialog("是否需要根据选择的实体类创建一个新的参数类?",
                                    "Yes", "No");
                    if (choose == 0) {
                        //创建新的参数类
                        PsiJavaFile containingFile = (PsiJavaFile) chooseEntityClass.getContainingFile();
                        EntityTransformHandler.entityToParam(project, containingFile,
                                methodName.getText(), methodDescribe.getText(), false,
                                buildParamClassName -> {
                                    setInputParamValue(buildParamClassName);
                                });
                        return;
                    }
                }
                frameVisibleHandle(() -> {
                    PsiClass psiClass = choosePsiClass(project, serviceClass, "param");
                    if (psiClass != null) {
                        setInputParamValue(psiClass.getQualifiedName());
                    }
                }, jFrame);

            }
        });
        //出参
        this.outputChooseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                frameVisibleHandle(() -> {
                    PsiClass psiClass = choosePsiClass(project, serviceClass, "Result");
                    if (psiClass != null) {
                        setOutputParamValue(psiClass.getQualifiedName());
                    }
                }, jFrame);
            }
        });

        OKButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    HandleServiceMethod method = buildBean();
                    ServiceMethodHandler.createMethodJavaCode(project, serviceClass, method);
                    jFrame.dispose();
                } catch (RuntimeException exception) {
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
     * 根据窗体中的组件内容 生成 处理服务方法Bean 用于生成方法Java 代码片段
     *
     * @return
     */
    private HandleServiceMethod buildBean() {
        HandleServiceMethod bean = new HandleServiceMethod();
        bean.setMethodName(methodName.getText());
        bean.setDescription(methodDescribe.getText());
        bean.setHandleType(methodHandleType);
        if (chooseEntityClass == null) {
            throw new RuntimeException("创建模版方法必须选择关联的实体类");
        }
        bean.setRelationEntity(chooseEntityClass.getQualifiedName());
        bean.setRelationEntityDescription(PsiHelper.getEntityDescription(chooseEntityClass));
        bean.setInputParam(inputParamValue);
        String selectedItem = (String) outputParam.getSelectedItem();
        if ("void".equals(selectedItem) || "int".equals(selectedItem)) {
            bean.setOutputParam(selectedItem);
        } else {
            bean.setOutputParam(outputParamValue);
        }
        if (MethodHandleType.DELETE.equals(methodHandleType)
                || MethodHandleType.UPDATE.equals(methodHandleType)) {
            //条件
            conditionFieldsTableHandle.updateValues(this.conditionTable);
            bean.setConditionFields(conditionFieldsTableHandle.getDataList());
            //更新字段
            if (MethodHandleType.UPDATE.equals(methodHandleType)) {
                updateFieldsTableHandle.updateValues(this.updateTable);
                bean.setUpdateFields(updateFieldsTableHandle.getDataList());
            }
        }
        bean.setCheckExists(checkRecordIsExistsCheckBox.isSelected());
        return bean;
    }

    /**
     * 获取PsiClass
     *
     * @param project
     * @param serviceClass
     * @return
     */
    private PsiClass choosePsiClass(Project project, PsiClass serviceClass, String directory) {
        TreeJavaClassChooserDialog selector = new TreeJavaClassChooserDialog(
                String.format("请选择一个[%s]类", DJJHelper.firstToUpperCase(directory)), project);
        PsiDirectory containingDirectory = serviceClass.getContainingFile().getContainingDirectory();
        PsiDirectory paramDirectory = containingDirectory.findSubdirectory(directory);
        if (paramDirectory == null) {
            paramDirectory = containingDirectory;
        }
        selector.selectDirectory(paramDirectory);
        selector.show();
        PsiClass psiClass = selector.getSelected();
        selector.dispose();
        return psiClass;
    }

    //标题处理
    private void titleHandle(PsiClass serviceClass) {
        String className = serviceClass.getName();
        titleLabel.setText("Add a method to " + className);
    }

    //方法处理
    private void methodHandle() {
        if (chooseEntityClass == null) {
            return;
        }
        String methodName = methodHandleType.getMethodName(chooseEntityClass.getName());
        String methodDescription = methodHandleType.getMethodDescription(PsiHelper.getEntityDescription(chooseEntityClass));

        this.methodName.setText(methodName);
        this.methodDescribe.setText(methodDescription);
        setRelationEntityValue(chooseEntityClass.getQualifiedName());

        this.outputParam.removeAllItems();
        this.outputParam.addItem("void");
        switch (methodHandleType) {
            case DELETE:
            case UPDATE:
                this.outputParam.addItem("int");
                this.outputParam.setSelectedItem("int");
                break;
            case SAVE:
                this.setOutputParamValue(chooseEntityClass.getQualifiedName());
                break;
        }
        this.handleTable();
    }

    //额外面板是否显示
    private void otherPanelVisible(JFrame jFrame, JRadioButton... radioButtons) {
        for (JRadioButton radioButton : radioButtons) {
            radioButton.addChangeListener(e -> {
                double height = defaultHeight;
                updatePanel.setVisible(false);
                conditionPanel.setVisible(false);
                if (saveRadioButton.isSelected()) {
                    methodHandleType = MethodHandleType.SAVE;
                }
                if (updateRadioButton.isSelected()) {
                    updatePanel.setVisible(true);
                    conditionPanel.setVisible(true);
                    methodHandleType = MethodHandleType.UPDATE;
                    height += 270;
                }
                if (deleteRadioButton.isSelected()) {
                    conditionPanel.setVisible(true);
                    methodHandleType = MethodHandleType.DELETE;
                    height += 160;
                }
                //处理方法
                methodHandle();
                //重新设置窗体高度
                jFrame.setSize(new Dimension(jFrame.getWidth(), Double.valueOf(height).intValue()));
            });
        }
    }

    private void setRelationEntityValue(String relationEntityValue) {
        this.relationEntityValue = relationEntityValue;
        this.relationEntity.setText(DJJHelper.getSimpleName(relationEntityValue));
    }

    private void setInputParamValue(String inputParamValue) {
        this.inputParamValue = inputParamValue;
        this.inputParam.setText(DJJHelper.getSimpleName(inputParamValue));
    }

    private void setOutputParamValue(String outputParamValue) {
        this.outputParamValue = outputParamValue;
        String simpleName = DJJHelper.getSimpleName(outputParamValue);
        this.outputParam.addItem(simpleName);
        this.outputParam.setSelectedItem(simpleName);
    }

    /**
     * 处理表格
     */
    private void handleTable() {
        if (MethodHandleType.SAVE.equals(methodHandleType)) {
            return;
        }
        List<HandleNonEditableField> conditionFields = new ArrayList<>();
        List<HandleNonEditableField> updateFields = new ArrayList<>();
        if (MethodHandleType.UPDATE.equals(methodHandleType)) {
            updateFields = new ArrayList<>();
        }
        PsiField[] psiFields = chooseEntityClass.getFields();
        for (PsiField psiField : psiFields) {
            HandleNonEditableField methodField = new HandleNonEditableField(psiField);
            if (methodField.getDescription() == null) {
                continue;
            }
            conditionFields.add(methodField);
            if (MethodHandleType.UPDATE.equals(methodHandleType)) {
                updateFields.add(new HandleNonEditableField(psiField));
            }
        }
        NonEditableFieldTable table = (NonEditableFieldTable) this.conditionTable;
        this.conditionFieldsTableHandle = new TableDataHandle<>(HandleNonEditableField.class,
                conditionFields, false);
        table.updateHandle(this.conditionFieldsTableHandle);

        if (MethodHandleType.UPDATE.equals(methodHandleType)) {
            NonEditableFieldTable methodTable = (NonEditableFieldTable) this.updateTable;
            this.updateFieldsTableHandle = new TableDataHandle<>(HandleNonEditableField.class,
                    updateFields, false);
            methodTable.updateHandle(this.updateFieldsTableHandle);
        }
    }

    private void createUIComponents() {
        TableDataHandle<HandleNonEditableField> dataHandle = new TableDataHandle<>(HandleNonEditableField.class,
                new ArrayList<>(), false);
        this.conditionTable = new NonEditableFieldTable(dataHandle);
        this.updateTable = new NonEditableFieldTable(dataHandle);
    }

}

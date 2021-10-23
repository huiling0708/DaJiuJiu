package main.java.form;

import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.TreeJavaClassChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.search.GlobalSearchScope;
import main.java.bean.HandleNonEditableField;
import main.java.common.DJJMessage;
import main.java.common.PsiHelper;
import main.java.common.TableDataHandle;
import main.java.common.UseNeedImportClass;
import main.java.component.BaseDJJForm;
import main.java.component.NonEditableFieldTable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 类添加字段窗体
 */
public class ClassAddFieldForm extends BaseDJJForm {
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JTable classFieldsTable;
    private JTable addFieldsTable;
    private JButton OKButton;
    private JButton cancelButton;

    private JComboBox chooseClassBox;
    private JButton chooseButton;
    private JButton addButton;
    private JButton removeButton;

    private TableDataHandle<HandleNonEditableField> classFieldsHandle;
    private TableDataHandle<HandleNonEditableField> addFieldsHandle;

    public ClassAddFieldForm(Project project, PsiClass paramClass, Consumer<List<HandleNonEditableField>> consumer) {
        JFrame jFrame = this.windowCenter("Add Field To Class", this.mainPanel);
        jFrame.getRootPane().setDefaultButton(OKButton);
        this.titleHandle(paramClass);

        //选择实体类
        chooseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frameVisibleHandle(() -> {
                    chooseClassHandle(project, paramClass);
                }, jFrame);
            }
        });
        //选择监听
        chooseClassBox.addItemListener(e -> {
            String item = (String) e.getItem();
            if (ItemEvent.SELECTED == e.getStateChange()) {
                PsiClassBox box = (PsiClassBox) chooseClassBox;
                PsiClass psiClass = box.getPsiClass(item);
                updateClassFieldTableHandle(psiClass);
            }
        });

        // 添加字段按钮监听 把选择的字段添加到 addFields表格中
        addButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addFieldButtonHandle();
            }
        });
        //移除button按钮处理
        removeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                removeButtonHandle();
            }
        });

        OKButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    okHandle(paramClass, consumer);
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

    //标题处理
    private void titleHandle(PsiClass psiClass) {
        String className = psiClass.getName();
        titleLabel.setText("Add Fields to " + className);
    }

    /**
     * ok 处理
     *
     * @param consumer
     */
    private void okHandle(PsiClass psiClass, Consumer<List<HandleNonEditableField>> consumer) {
        this.addFieldsHandle.updateValues(this.addFieldsTable);
        List<HandleNonEditableField> dataList = this.addFieldsHandle.getDataList();
        if (dataList.size() == 0) {
            throw new RuntimeException("未添加字段");
        }
        Set<String> fieldNameSet = new HashSet<>();
        for (PsiField field : psiClass.getFields()) {
            fieldNameSet.add(field.getName());
        }
        int selectCount = 0;
        int existFieldCount = 0;
        for (HandleNonEditableField field : dataList) {
            //未选中
            if (!field.isSelected()) {
                continue;
            }
            selectCount++;
            if (!fieldNameSet.add(field.getName())) {
                existFieldCount++;
                field.setSelected(false);
            }
        }
        if (selectCount == 0) {
            throw new RuntimeException("至少勾选一个字段");
        }
        if (selectCount <= existFieldCount) {
            throw new RuntimeException(String.format("勾选的字段在[%s]类中都已存在", psiClass.getName()));
        }
        consumer.accept(dataList);
        DJJMessage.messageDialog("添加成功!");
        NonEditableFieldTable downTable = (NonEditableFieldTable) this.addFieldsTable;
        downTable.getHandle().getDataList().clear();
        downTable.updateHandle();
    }

    /**
     * 移除按钮
     */
    private void removeButtonHandle() {
        NonEditableFieldTable downTable = (NonEditableFieldTable) this.addFieldsTable;
        downTable.getHandle().updateValues(downTable);
        List<HandleNonEditableField> dataList = downTable.getHandle().getDataList();
        Iterator<HandleNonEditableField> iterator = dataList.iterator();
        while (iterator.hasNext()) {
            HandleNonEditableField field = iterator.next();
            if (field.isSelected()) {
                iterator.remove();
            }
        }
        downTable.updateHandle();
    }

    /**
     * 添加字段按钮处理
     */
    private void addFieldButtonHandle() {
        //把上面表格（选择的类字段表格）选择的字段添加到下面表格（待添加的字段）中
        NonEditableFieldTable topTable = (NonEditableFieldTable) this.classFieldsTable;
        NonEditableFieldTable downTable = (NonEditableFieldTable) this.addFieldsTable;
        topTable.getHandle().updateValues(topTable);
        downTable.getHandle().updateValues(downTable);
        List<HandleNonEditableField> topList = topTable.getHandle().getDataList();
        List<HandleNonEditableField> downList = downTable.getHandle().getDataList();

        Set<String> nameSet = downList.stream().map(HandleNonEditableField::getName)
                .collect(Collectors.toSet());
        for (HandleNonEditableField field : topList) {
            if (!field.isSelected()) {
                continue;
            }
            if (nameSet.contains(field.getName())) {
                continue;
            }
            downList.add(field);
        }
        downTable.updateHandle();
    }

    /**
     * 选择class处理
     *
     * @param project
     * @param paramClass
     */
    private void chooseClassHandle(Project project, PsiClass paramClass) {
        TreeJavaClassChooserDialog selector = new TreeJavaClassChooserDialog("请选择一个类(模型)",
                project, GlobalSearchScope.projectScope(project), new ClassFilter() {
            @Override
            public boolean isAccepted(PsiClass psiClass) {
                if (paramClass.getQualifiedName()
                        .equals(psiClass.getQualifiedName())) {
                    return false;
                }
                return psiClass.getAnnotation(
                        UseNeedImportClass.ApiModel.getImportContent()) != null;
            }
        }, null);
        selector.selectDirectory(paramClass.getContainingFile().getParent());
        selector.show();
        PsiClass psiClass = selector.getSelected();
        selector.dispose();
        if (psiClass == null) {
            return;
        }
        //设置下拉框
        PsiClassBox box = (PsiClassBox) chooseClassBox;
        box.addPsiItem(psiClass);
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
        List<HandleNonEditableField> list =
                HandleNonEditableField.buildFieldList(psiClass);
        this.classFieldsHandle.setDataList(list);
        NonEditableFieldTable table = (NonEditableFieldTable) this.classFieldsTable;
        table.updateHandle();
    }

    private void createUIComponents() {
        chooseClassBox = new PsiClassBox();

        this.classFieldsHandle = new TableDataHandle<>(
                HandleNonEditableField.class, new ArrayList<>(), false);
        this.addFieldsHandle = new TableDataHandle<>(
                HandleNonEditableField.class, new ArrayList<>(), false);
        this.classFieldsTable = new NonEditableFieldTable(this.classFieldsHandle);
        this.addFieldsTable = new NonEditableFieldTable(this.addFieldsHandle)
                .removeNonEditableColumn(1, 2, 3);
    }

    private class PsiClassBox extends JComboBox {

        private Map<String, PsiClass> psiKeyMap = new HashMap<>();

        public void addPsiItem(PsiClass psiClass) {
            String name = psiClass.getName();
            String description = PsiHelper.getApiModelDescription(psiClass);
            super.addItem(name);
            this.setSelectedItem(name);
            this.setToolTipText(description);
            psiKeyMap.put(name, psiClass);

        }

        public PsiClass getPsiClass(String item) {
            return psiKeyMap.get(item);
        }
    }
}

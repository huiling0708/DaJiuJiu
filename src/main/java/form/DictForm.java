package main.java.form;

import com.intellij.openapi.project.Project;
import main.java.bean.DictBean;
import main.java.bean.DictValueBean;
import main.java.common.DJJHelper;
import main.java.common.DJJMessage;
import main.java.common.TableDataHandle;
import main.java.common.UseNeedImportClass;
import main.java.component.BaseDJJForm;
import main.java.component.DictValueTable;
import main.java.transform.DataTransformEntityHandler;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DictForm extends BaseDJJForm {
    private JTextField nameText;
    private JTextField describeText;
    private JTextField packageText;
    private JButton removeLastButton;
    private JButton addDictValueButton;
    private JTable valuesTable;
    private JButton OKButton;
    private JButton cancelButton;
    private JPanel mainPanel;

    private TableDataHandle<DictValueBean> dataHandle;
    private boolean showMessage;//展示提示消息

    public DictForm(Project project) {
        this(project, null, null, dictBean -> {
        });
        showMessage = true;
    }

    public DictForm(Project project, String name, String describe, Consumer<DictBean> consumer) {
        if (!DJJHelper.isBlank(name)) {
            nameText.setText(DJJHelper.firstToUpperCase(name));
        }
        if (!DJJHelper.isBlank(describe)) {
            describeText.setText(describe);
        }
        packageText.setText(UseNeedImportClass.DictPackage.getImportContent(project));

        JFrame jFrame = this.windowCenter("Creat Dict", this.mainPanel);
        jFrame.getRootPane().setDefaultButton(OKButton);
        //表格
        DictValueTable dictValueTable = (DictValueTable) valuesTable;
        dictValueTable.setProject(project);

        //添加 Value
        addDictValueButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dataHandle.updateValues(valuesTable);
                dataHandle.getDataList().add(new DictValueBean(String.valueOf(
                        dataHandle.getDataList().size() + 1)));
                dictValueTable.updateHandle(dataHandle);

            }
        });
        //移除最后一个
        removeLastButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dataHandle.updateValues(valuesTable);
                dataHandle.getDataList().remove(dataHandle.getDataList().size() - 1);
                dictValueTable.updateHandle(dataHandle);
            }
        });

        OKButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DictBean bean = buildDictBean();
                try {
                    DataTransformEntityHandler.createDict(project, bean);
                    consumer.accept(bean);
                    jFrame.dispose();
                    if (showMessage){
                        DJJMessage.messageDialog("创建成功！");
                    }
                } catch (Exception exception) {
                    consumer.accept(null);
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
     * 生成字典bean
     *
     * @return
     */
    private DictBean buildDictBean() {
        dataHandle.updateValues(this.valuesTable);
        DictBean bean = new DictBean();
        bean.setDictName(this.nameText.getText());
        bean.setDictDescribe(this.describeText.getText());
        bean.setValues(dataHandle.getDataList());
        return bean;
    }

    private void createUIComponents() {
        List<DictValueBean> list = new ArrayList<>();
        list.add(new DictValueBean("1"));
        this.dataHandle = new TableDataHandle<>(DictValueBean.class, list, false, false);
        this.valuesTable = new DictValueTable(dataHandle);
    }
}

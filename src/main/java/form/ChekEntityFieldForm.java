package main.java.form;

import com.intellij.openapi.project.Project;
import main.java.bean.VoClassBean;
import main.java.bean.VoClassFormBean;
import main.java.common.DJJMessage;
import main.java.common.TableDataHandle;
import main.java.component.BaseDJJForm;
import main.java.component.VoClassAddFieldTable;
import main.java.transform.ChekEntityFieldHandler;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查实体字段窗体
 * 用于向Vo类中添加实体字段
 */
public class ChekEntityFieldForm extends BaseDJJForm {
    private JPanel mainPanel;
    private JTextField entityText;
    private JTextField fieldText;
    private JButton cancelButton;
    private JButton OKButton;
    private JButton showOtherVoClassesButton;
    private JTable otherVoTable;
    private JTable needAddTable;
    private JPanel otherPanel;

    private TableDataHandle<VoClassBean> addFieldHandle;

    public ChekEntityFieldForm(Project project, VoClassFormBean formBean) {

        JFrame jFrame = this.windowCenter("Add Field To Vo Class", this.mainPanel);
        jFrame.getRootPane().setDefaultButton(OKButton);
        this.entityText.setText(formBean.getEntityText());
        this.fieldText.setText(formBean.getFieldText());

        //需添加字段的table
        this.addFieldHandle = new TableDataHandle<>(VoClassBean.class,
                formBean.getNeedAddFieldVoClasses());
        //更新两个表格的数据源
        VoClassAddFieldTable table = (VoClassAddFieldTable) this.needAddTable;
        table.updateHandle(this.addFieldHandle);
        //其他vo table
        VoClassAddFieldTable otherFieldTable = (VoClassAddFieldTable) this.otherVoTable;
        otherFieldTable.updateHandle(new TableDataHandle<>(VoClassBean.class,
                formBean.getOtherVoClasses(), false, true));

        //打开或关闭 其它引用面板
        //其它引用面板展示的内容为 其它引用了该实体，但已经包含了指定字段的Vo类
        showOtherVoClassesButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (otherPanel.isVisible()) {
                    otherPanel.setVisible(false);
                    showOtherVoClassesButton.setText("Show Other Vo Classes");
                } else {
                    otherPanel.setVisible(true);
                    showOtherVoClassesButton.setText("Close Other Vo Classes");
                }
            }
        });

        OKButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (okHandle(project, formBean)) {
                    jFrame.dispose();
                    DJJMessage.messageDialog("添加成功！");
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
     * ok 处理
     *
     * @return 返回ture 则jFrame.dispose()
     */
    private boolean okHandle(Project project, VoClassFormBean formBean) {
        List<VoClassBean> dataList = addFieldHandle.getDataList();
        if (dataList == null || dataList.size() == 0) {
            return true;
        }
        addFieldHandle.updateValues(this.needAddTable);
        int chooseCount = 0;
        StringBuilder sub = new StringBuilder();
        for (VoClassBean voClassBean : dataList) {
            if (!voClassBean.isSelected()) {
                continue;
            }
            if (chooseCount != 0) {
                sub.append("、");
            }
            sub.append(voClassBean.getClassName());
            chooseCount++;
        }
        if (chooseCount == 0) {
            return true;
        }
        String countMessage = chooseCount == 1 ? "" : (chooseCount + "");
        String message = String.format("确定需要把字段[%s]添加到%s这%s个类中吗?",
                formBean.getFieldName(), sub.toString(), countMessage);
        int i = DJJMessage.chooseOptionDialog(message, "是的", "不,我重新选");
        if (i != 0) {
            return false;
        }
        ChekEntityFieldHandler.callBackHandle(project, formBean);
        return true;
    }

    private void createUIComponents() {
        this.needAddTable = new VoClassAddFieldTable(
                new TableDataHandle<>(VoClassBean.class,
                        new ArrayList<>())
        );
        this.otherVoTable = new VoClassAddFieldTable(
                new TableDataHandle<>(VoClassBean.class,
                        new ArrayList<>(), false, true)
        );
    }
}

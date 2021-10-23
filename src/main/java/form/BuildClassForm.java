package main.java.form;

import main.java.common.DJJMessage;
import main.java.common.IButtonEvent;
import main.java.common.IClassForm;
import main.java.component.BaseDJJForm;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 生成 java class类窗体
 */
public class BuildClassForm extends BaseDJJForm {
    private JPanel panel1;
    private JButton cancelButton;
    private JButton OKButton;
    private JTextField classNameText;
    private JTextField descriptionText;
    private JButton fieldsButton;


    /**
     * @param classTable        类窗体接口 用于接收 类名称、描述和窗体标题
     * @param okButtonEvent     点击OK 按钮后的事件响应
     * @param fieldsButtonEvent 点击 fields 按钮时的事件响应
     */
    public BuildClassForm(IClassForm classTable, IButtonEvent okButtonEvent, IButtonEvent fieldsButtonEvent) {
        this.classNameText.setText(classTable.getClassName());
        this.descriptionText.setText(classTable.getClassDescription());

        JFrame jFrame = this.windowCenter(classTable.getTitle(), this.panel1);
        jFrame.getRootPane().setDefaultButton(OKButton);

        if (fieldsButtonEvent.buttonName() != null) {
            fieldsButton.setText(fieldsButtonEvent.buttonName());
        }

        fieldsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fieldsButtonEvent.handle(jFrame, e);
            }
        });

        OKButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean handle = okButtonEvent.handle(e, jFrame, classNameText, descriptionText);
                if (handle) {
                    jFrame.dispose();
                    if (classTable.showMessage()) {
                        DJJMessage.messageDialog("创建成功！");
                    }
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
}

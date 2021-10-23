package main.java.form;

import main.java.bean.HandleClassField;
import main.java.bean.QueryFieldBean;
import main.java.common.UseNeedImportClass;
import main.java.component.BaseDJJForm;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * QueryProvide注解设置窗体
 * 用于在生成Vo类时，点击字段详情的Handle Button展示的内容
 */
public class QueryProvideForm extends BaseDJJForm {
    private JCheckBox thisIsAQueryCheckBox;
    private JCheckBox mustInputCheckBox;
    private JCheckBox queryNullableCheckBox;
    private JComboBox sqlExpression;
    private JComboBox presentCondition;
    private JTextField fixedCondition;
    private JButton OKButton;
    private JButton cancelButton;
    private JPanel panel;

    public QueryProvideForm(HandleClassField handleClassField, Consumer consumer) {
        JFrame jFrame = this.windowCenter(handleClassField.getName() + " query set up", this.panel);
        jFrame.getRootPane().setDefaultButton(OKButton);

        this.initValue(handleClassField);//初始化

        thisIsAQueryCheckBox.addChangeListener(e -> {
            Object source = e.getSource();
            if (source.equals(thisIsAQueryCheckBox)) {
                setElementEnable();
            }
        });

        OKButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setValue(handleClassField);
                consumer.accept(handleClassField.getAnnotationContent());
                jFrame.dispose();
            }
        });


        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                jFrame.dispose();
            }
        });
    }

    private void initValue(HandleClassField handle) {
        QueryFieldBean bean = handle.getQueryFieldBean();
        if (bean == null) {
            bean = new QueryFieldBean();
        }
        this.thisIsAQueryCheckBox.setSelected(handle.isExistAnn());
        this.mustInputCheckBox.setSelected(bean.isMustInput());
        this.queryNullableCheckBox.setSelected(bean.isQueryNullable());
        this.sqlExpression.setSelectedItem(bean.getCondition());
        this.presentCondition.setSelectedItem(bean.getPresent());
        this.fixedCondition.setText(bean.getFixedValue());
        this.setElementEnable();
    }

    private void setValue(HandleClassField handle) {
        if (!this.thisIsAQueryCheckBox.isSelected()) {
            handle.setQueryFieldBean(null);
            handle.setAnnotationContent("");
            return;
        }
        QueryFieldBean bean = handle.getQueryFieldBean();
        if (bean == null) {
            bean = new QueryFieldBean();
        }
        handle.setAnnotationContent(UseNeedImportClass.QueryField.name());

        bean.setMustInput(this.mustInputCheckBox.isSelected());
        bean.setQueryNullable(this.queryNullableCheckBox.isSelected());
        bean.setCondition(this.sqlExpression.getSelectedItem().toString());
        bean.setPresent(this.presentCondition.getSelectedItem().toString());
        bean.setFixedValue(this.fixedCondition.getText());

        handle.setQueryFieldBean(bean);
    }


    private void setElementEnable() {
        boolean enable = this.thisIsAQueryCheckBox.isSelected();
        this.mustInputCheckBox.setEnabled(enable);
        this.queryNullableCheckBox.setEnabled(enable);
        this.sqlExpression.setEnabled(enable);
        this.presentCondition.setEnabled(enable);
        this.fixedCondition.setEnabled(enable);
    }
}

package main.java.form;

import lombok.Getter;
import main.java.common.ICreateUI;
import main.java.common.ITableBean;
import main.java.common.TableDataHandle;
import main.java.component.BaseDJJForm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 字段表格窗体
 * 用于展示详情的表格
 *
 * @param <T>
 */
@Getter
public class UIFieldTable<T extends ITableBean> extends BaseDJJForm {
    private JPanel panel1;
    private JButton cancelButton;
    private JButton OKButton;
    private JTable table1;

    private TableDataHandle<T> handle;//表格数据源
    private ICreateUI createUI;//创建JTable接口 用于接收需要创建的表格样式

    public UIFieldTable(TableDataHandle<T> handle, ICreateUI createUI) {
        this(handle, new Dimension(600, 300), "字段详情", createUI);
    }

    public UIFieldTable(TableDataHandle<T> handle, Dimension size, String title, ICreateUI createUI) {
        super();
        this.handle = handle;
        this.createUI = createUI;

        JFrame jFrame = this.windowCenter(title, this.panel1, size);
        jFrame.getRootPane().setDefaultButton(OKButton);

        //设置表格窗体
        this.handle.setTableFrame(jFrame);

        this.cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                jFrame.dispose();
            }
        });

        this.OKButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handle.updateValues(table1);
                jFrame.dispose();
            }
        });
    }

    private void createUIComponents() {
        this.table1 = createUI.createTable(this.handle);
    }
}

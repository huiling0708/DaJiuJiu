package main.java.component;

import main.java.common.DJJMessage;
import main.java.common.ITableBean;
import main.java.common.TableDataHandle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 基础表格
 *
 * @param <T>
 */
public abstract class BaseDJJTable<T extends ITableBean> extends JTable {

    protected TableDataHandle<T> handle;//数据源

    public BaseDJJTable(TableDataHandle<T> handle) {
        super(new DefaultTableModel(handle.getRows(), handle.getColumnNames()) {
            @Override
            public Class getColumnClass(int c) {
                //重写getColumnClass 方法，编辑器才能拿到被修改成容器的表头
                Object value = getValueAt(0, c);
                if (value != null) {
                    return value.getClass();
                }
                return super.getClass();
            }
        });
        this.handle = handle;
        this.init();
        this.getTableHeader().setReorderingAllowed(false);//整列不能拖动
    }

    /**
     * 获取数据源
     *
     * @return
     */
    public TableDataHandle<T> getHandle() {
        return handle;
    }

    /**
     * 更新数据源（重写加载数据）
     */
    public void updateHandle() {
        this.updateHandle(this.handle);
    }

    /**
     * 更新数据源
     *
     * @param handle
     */
    public void updateHandle(TableDataHandle<T> handle) {
        this.handle = handle;
        DefaultTableModel model = (DefaultTableModel) this.getModel();
        model.getDataVector().clear();
        model.getDataVector().addAll(handle.getRows());
        this.updateUI();
    }

    /**
     * 指定列是否可以编辑
     *
     * @param row
     * @param column
     * @return
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        if (handle.getNotEditableCells().contains(column)) {
            return false;
        }
        return super.isCellEditable(row, column);
    }

    /**
     * 表格初始化
     */
    protected void init() {
        this.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);//单行选择
        this.setShowGrid(false);//不显示网格

        //第一列为选择列 如果需要，把第一列的表头渲染为 JCheckBox
        if (handle.isNeedSelect()) {
            this.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(this, handle.getDataList()));
            this.getColumnModel().getColumn(0).setCellRenderer(new CheckBoxRenderer());
            this.getColumnModel().getColumn(0).setCellEditor(new CheckBoxCellEditor());
        }

        //最后一列为操作详情列 如果需要 把最后一列渲染为 JCheckButton
        if (handle.isNeedHandleButton()) {
            IButtonEvent event = new IButtonEvent() {
                @Override
                public void invoke(ActionEvent e) {
                    TableButton button = (TableButton) e.getSource();
                    fieldButtonHandle(button);
                }
            };
            int lastColumnFlag = handle.getColumnNames().size() - 1;
            this.getColumnModel().getColumn(lastColumnFlag).setCellRenderer(new ButtonRender());
            this.getColumnModel().getColumn(lastColumnFlag).setCellEditor(new ButtonEditor(event));
        }
        //设置列宽
        handle.setPreferredWidth(this);
        this.getTableHeader().repaint();
    }

    /**
     * 点击最后一列的按钮事件监听
     *
     * @param button
     */
    protected void fieldButtonHandle(TableButton button) {
        DJJMessage.errorDialog("详情功能尚未开放!");
    }

    /**
     * 表头渲染器 把该列渲染为 JCheckBox
     */
    protected class TableHeaderRenderer implements TableCellRenderer {

        protected JCheckBox checkBox;

        public TableHeaderRenderer(JTable jTable, List<T> dataList) {
            checkBox = new JCheckBox();
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            checkBox.setSelected(this.initSelect(dataList));
            //表头上添加监听
            jTable.getTableHeader().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 0) {
                        int atPoint = jTable.columnAtPoint(e.getPoint());
                        if (atPoint == 0) {
                            boolean selected = !checkBox.isSelected();
                            checkBox.setSelected(selected);
                            //把每一列勾选情况修改为和该列表头一致
                            for (int i = 0; i < jTable.getRowCount(); i++) {
                                jTable.setValueAt(selected, i, 0);
                            }
                            tableHeader.repaint();
                        }
                    }
                }
            });
        }

        /**
         * 初始化选择
         *
         * @param dataList
         * @return
         */
        private boolean initSelect(List<T> dataList) {
            //如果数据列表中有一个被勾选了，则返回 true
            for (T t : dataList) {
                if (t.isSelected()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String valueStr = (String) value;
            JLabel label = new JLabel(valueStr);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            checkBox.setBorderPainted(true);
            JComponent component = (column == 0) ? checkBox : label;
            component.setForeground(tableHeader.getForeground());
            component.setBackground(tableHeader.getBackground());
            component.setFont(tableHeader.getFont());
            component.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            return component;
        }
    }

    /**
     * JCheckBox 列编辑器
     */
    protected class CheckBoxCellEditor extends AbstractCellEditor implements TableCellEditor {

        protected JCheckBox checkBox;

        public CheckBoxCellEditor() {
            checkBox = new JCheckBox();
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            checkBox.setSelected(((Boolean) value).booleanValue());
            return checkBox;
        }

        @Override
        public Object getCellEditorValue() {
            return Boolean.valueOf(checkBox.isSelected());
        }
    }

    /**
     * JCheckBox 列渲染器
     */
    protected class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

        public CheckBoxRenderer() {
            super();
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            if (value instanceof Boolean) {
                setSelected(((Boolean) value).booleanValue());
                setEnabled(table.isCellEditable(row, column));
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            return this;
        }
    }

    /**
     * 按钮事件 点击按钮后调用
     */
    protected interface IButtonEvent {
        void invoke(ActionEvent e);
    }

    /**
     * 在table 中显示的button 能够返回butoon 所在行数
     */
    protected class TableButton extends JButton {
        private int row;

        public TableButton(String text) {
            super(text);
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }
    }

    /**
     * Button 渲染器
     */
    protected class ButtonRender implements TableCellRenderer {
        private JButton button;

        public ButtonRender() {
            button = new JButton("=");
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return button;
        }
    }

    /**
     * Button 编辑器
     */
    protected class ButtonEditor extends DefaultCellEditor {

        private TableButton button;

        public ButtonEditor(IButtonEvent event) {
            super(new JTextField());
            button = new TableButton("=");
            button.addActionListener(e -> event.invoke(e));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            setClickCountToStart(0);
            button.setRow(row);
            return button;
        }
    }
}
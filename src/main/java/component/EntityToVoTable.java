package main.java.component;


import main.java.bean.HandleClassField;
import main.java.common.TableDataHandle;
import main.java.common.TypeMapping;
import main.java.form.QueryProvideForm;

import javax.swing.*;

/**
 * 实体类转换为Vo类表格
 */
public class EntityToVoTable extends BaseDJJTable<HandleClassField> {

    private final static JComboBox QUERY_ANNOTATION_COMBOBOX;

    static {
        QUERY_ANNOTATION_COMBOBOX = new JComboBox();
        QUERY_ANNOTATION_COMBOBOX.addItem("");
        QUERY_ANNOTATION_COMBOBOX.addItem("QueryField");
    }


    public EntityToVoTable(TableDataHandle<HandleClassField> handle) {
        super(handle);
    }

    @Override
    protected void init() {
        super.init();
        //第4列为Java类型下拉
        this.getColumnModel().getColumn(3).setCellEditor(
                new DefaultCellEditor(TypeMapping.getTypeComboBox()));
        //第5列为查询注解选择
        this.getColumnModel().getColumn(4).setCellEditor(
                new DefaultCellEditor(QUERY_ANNOTATION_COMBOBOX));
    }

    @Override
    protected void fieldButtonHandle(TableButton button) {
        //点击最后一列的button 打开 QueryProvideForm 配置查询条件
        String valueAt = (String) this.getValueAt(button.getRow(), 4);
        HandleClassField handleClassField = handle.getDataList().get(button.getRow());
        handleClassField.setAnnotationContent(valueAt);
        new QueryProvideForm(handleClassField, x -> {
            this.setValueAt(x, button.getRow(), 4);
        });
    }
}

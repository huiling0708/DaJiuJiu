package main.java.component;


import main.java.bean.HandleNonEditableField;
import main.java.common.TableDataHandle;

/**
 * 不可编辑的字段详情通用表格
 */
public class NonEditableFieldTable extends BaseDJJTable<HandleNonEditableField> {

    public NonEditableFieldTable(TableDataHandle<HandleNonEditableField> handle) {
        super(handle);
    }

    public NonEditableFieldTable removeNonEditableColumn(int... columns) {
        for (int column : columns) {
            handle.getNotEditableCells().remove(column);
        }
        return this;
    }
}

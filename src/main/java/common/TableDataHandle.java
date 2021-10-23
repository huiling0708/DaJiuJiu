package main.java.common;

import lombok.Getter;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 表格数据处理
 * 用于把数据源列表数据转换为 JTable 数据用于表格填充
 *
 * @param <T>
 */
@Getter
public class TableDataHandle<T extends ITableBean> {

    private List<T> dataList;//数据源
    private Class<T> dataClassType;//数据源类型
    private Vector columnNames;//表格列名称
    private List<DataCache> dataCaches;//数据解析映射
    private Set<Integer> notEditableCells;//不允许编辑的列
    private boolean needHandleButton;//是否需要有处理列 表格最后一列变为button
    private boolean needSelect;//是否需要有选择列 表格第一列变为勾选框

    private JFrame tableFrame;//表格存放的窗体 有特殊需求时使用

    public void setTableFrame(JFrame tableFrame) {
        this.tableFrame = tableFrame;
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
    }

    public TableDataHandle(Class<T> dataClassType, List<T> dataList) {
        this(dataClassType, dataList, true);
    }

    public TableDataHandle(Class<T> dataClassType, List<T> dataList, boolean needHandleButton) {
        this(dataClassType, dataList, true, needHandleButton);
    }

    public TableDataHandle(Class<T> dataClassType, List<T> dataList, boolean needSelect, boolean needHandleButton) {
        this.dataList = dataList;
        this.dataClassType = dataClassType;
        this.dataCaches = new ArrayList<>();
        this.columnNames = new Vector();
        this.notEditableCells = new HashSet<>();
        this.needHandleButton = needHandleButton;
        this.needSelect = needSelect;
        try {
            this.init();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("数据异常");
        }
    }


    private void init() throws IntrospectionException {
        //读取数据源类型 并获得ITableField注解类容 生成DataCache映射缓存
        BeanInfo beanInfo = Introspector.getBeanInfo(dataClassType);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor p : propertyDescriptors) {
            Field field;
            try {
                field = dataClassType.getDeclaredField(p.getName());
                field.setAccessible(true);
            } catch (NoSuchFieldException e) {
                continue;
            }
            ITableField annotation = field.getAnnotation(ITableField.class);
            if (annotation == null) {
                continue;
            }
            this.dataCaches.add(new DataCache(p, annotation));
        }
        this.dataCaches.sort(Comparator.comparing(DataCache::getSort));

        //生成列
        //第一列为选择列
        int sort = 0;
        if (needSelect) {
            columnNames.add("");
            sort++;
        }
        for (DataCache dataCache : this.dataCaches) {
            if (dataCache.notCellEditable) {
                notEditableCells.add(sort);
            }
            columnNames.add(dataCache.columnName);
            sort++;
        }
        if (needHandleButton) {
            //最后一列为操作列
            columnNames.add("Handle");
        }
    }

    /**
     * 设置列宽
     *
     * @param table
     */
    public void setPreferredWidth(JTable table) {
        int i = 0;
        if (needSelect) {
            table.getColumnModel().getColumn(i).setPreferredWidth(5);
            i++;
        }
        for (DataCache dataCache : this.dataCaches) {
            table.getColumnModel().getColumn(i).setPreferredWidth(dataCache.preferredWidth);
            i++;
        }
        if (needHandleButton) {
            table.getColumnModel().getColumn(i).setPreferredWidth(20);
        }
    }

    /**
     * 获取表格行数据
     *
     * @return
     */
    public Vector getRows() {
        Vector rows = new Vector();
        for (T t : this.dataList) {
            Vector row = new Vector();
            if (needSelect) {
                row.add(t.isSelected());
            }
            for (DataCache dataCache : dataCaches) {
                try {
                    row.add(dataCache.readMethod.invoke(t));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("解析数据异常");
                }
            }
            if (needHandleButton) {
                row.add(" ");
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * 更新表格数据
     * 把表格中的数据更新回数据源
     *
     * @param table
     */
    public void updateValues(JTable table) {
        TableCellEditor cellEditor = table.getCellEditor();
        if (cellEditor != null) {
            boolean b = cellEditor.stopCellEditing();
            if (!b) {
                throw new RuntimeException("表格编辑错误");
            }
        }
        int rowIndex = 0;
        for (T t : this.dataList) {
            int columnIndex = 0;
            if (needSelect){
                t.setSelected((Boolean) table.getValueAt(rowIndex, columnIndex));
                columnIndex++;
            }
            for (DataCache dataCache : this.dataCaches) {
                try {
                    dataCache.writeMethod.invoke(t, table.getValueAt(rowIndex, columnIndex));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("数据处理异常");
                }
                columnIndex++;
            }
            rowIndex++;
        }

    }

    /**
     * 映射缓存
     */
    private class DataCache {
        private int sort;//顺序
        private String columnName;//表格列名
        private int preferredWidth;//表格列宽度
        private Method readMethod;//数据源get方法
        private Method writeMethod;//数据源set方法
        private boolean notCellEditable;

        protected DataCache(PropertyDescriptor p, ITableField iTableField) {
            this.sort = iTableField.sort();
            this.columnName = iTableField.columnName();
            this.preferredWidth = iTableField.preferredWidth();
            this.readMethod = p.getReadMethod();
            this.writeMethod = p.getWriteMethod();
            this.notCellEditable = iTableField.notCellEditable();
        }

        public int getSort() {
            return sort;
        }
    }
}

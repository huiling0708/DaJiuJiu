package main.java.common;

/**
 * 表格bean
 * 实现该接口的bean可以生成JTable中的数据
 */
public interface ITableBean {

    boolean isSelected();

    void setSelected(boolean selected);

}

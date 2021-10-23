package main.java.common;

import javax.swing.*;

/**
 * 根据TableDataHandle 生成一个JTable
 *
 * @param <T>
 */
public interface ICreateUI<T extends ITableBean> {

    JTable createTable(TableDataHandle<T> handle);
}

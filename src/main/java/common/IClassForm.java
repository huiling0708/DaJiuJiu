package main.java.common;

/**
 * 类在窗体上显示
 */
public interface IClassForm {

    String getTitle();//标题

    String getClassName();//类名

    String getClassDescription();//类描述

    default boolean showMessage() {//是否展示消息
        return true;
    }
}

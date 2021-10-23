package main.java.common;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * button 事件处理
 */
public interface IButtonEvent {

    default void handle(MouseEvent e) {
    }

    default void handle(JFrame frame, MouseEvent e) {
        this.handle(e);
    }

    /**
     * 按钮名称默认为 fields 可以通过重写改方法来修改按钮的名称如 methods
     *
     * @return
     */
    default String buttonName() {
        return null;
    }

    default boolean handle(MouseEvent e, JFrame jFrame, JTextField classNameText, JTextField descriptionText) {
        return true;
    }
}

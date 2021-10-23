package main.java.common;


import com.intellij.openapi.ui.Messages;

import javax.swing.*;

/**
 * 对话消息
 */
public abstract class DJJMessage {

    private final static String DIALOG_TITLE = "DA JIU JIU";

    /**
     * 错误消息
     *
     * @param message
     */
    public static void errorDialog(String message) {
        Messages.showErrorDialog(message, DIALOG_TITLE);
    }

    /**
     * 错误消息
     *
     * @param e
     */
    public static void errorDialog(Exception e) {
       e.printStackTrace();
        errorDialog(e.getMessage());
    }

    /**
     * 提示消息
     * @param message
     */
    public static void messageDialog(String message){
        Messages.showMessageDialog(message,DIALOG_TITLE,Messages.getInformationIcon());
    }
    /**
     * 用户输入消息
     *
     * @param message
     * @param initialValue
     * @return
     */
    public static String inputDialog(String message, String initialValue) {
        return Messages.showInputDialog(message,
                DIALOG_TITLE,
                Messages.getInformationIcon(),
                initialValue, null);
    }

    /**
     * 用户选择 下拉选择
     *
     * @param message
     * @param values
     * @return
     */
    public static String chooseListDialog(String message, String... values) {
        return Messages.showEditableChooseDialog(message,
                DIALOG_TITLE,
                Messages.getQuestionIcon(),
                values, values[0], null);
    }

    /**
     * 用户选择 选项选择
     *
     * @param message
     * @param values
     * @return
     */
    public static int chooseOptionDialog(String message, String... values) {
        return Messages.showDialog(message, DIALOG_TITLE, values,
                0, Messages.getQuestionIcon());
    }
}

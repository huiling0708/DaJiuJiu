package main.java.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import main.java.common.DJJMessage;
import main.java.form.DictForm;

/**
 * 创建数据字典
 */
public class CreateDict extends BaseDirectoryAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            new DictForm(e.getProject());
        } catch (Exception exception) {
            DJJMessage.errorDialog(exception);
        }
    }

    @Override
    protected String getDirectoryName() {
        return "dict";
    }
}

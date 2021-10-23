package main.java.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import main.java.common.DJJMessage;
import main.java.transform.DataTransformEntityHandler;

/**
 * 根据输入的SQL语句生成实体类
 */
public class BuildEntityFromSQL extends BaseDirectoryAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            DataTransformEntityHandler.sqlToEntity(e);
        } catch (Exception exception) {
            DJJMessage.errorDialog(exception);
        }
    }

    @Override
    protected String getDirectoryName() {
        return "entity";
    }
}

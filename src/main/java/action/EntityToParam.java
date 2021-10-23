package main.java.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import main.java.common.DJJMessage;
import main.java.transform.EntityTransformHandler;

/**
 * 实体转换为 Param参数类
 */
public class EntityToParam extends BaseEntityFileAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            EntityTransformHandler.entityToParam(e);
        } catch (Exception exception) {
            DJJMessage.errorDialog(exception);
        }
    }
}

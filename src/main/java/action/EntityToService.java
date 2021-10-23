package main.java.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import main.java.common.DJJMessage;
import main.java.transform.EntityTransformHandler;

/**
 * 实体转换为 Service服务类
 */
public class EntityToService extends BaseEntityFileAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            EntityTransformHandler.entityToService(e);
        } catch (Exception exception) {
            DJJMessage.errorDialog(exception);
        }
    }
}

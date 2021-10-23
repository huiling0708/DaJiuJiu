package main.java.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import main.java.common.DJJMessage;
import main.java.transform.EntityTransformHandler;

/**
 * 实体转换为 Vo查询视图类
 */
public class EntityToVo extends BaseEntityFileAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            EntityTransformHandler.entityToVo(e);
        } catch (Exception exception) {
            DJJMessage.errorDialog(exception);
        }
    }
}

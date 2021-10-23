package main.java.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import main.java.common.DJJMessage;
import main.java.transform.DataTransformEntityHandler;

/**
 * 根据选择的pdm文件生成实体类
 */
public class BuildEntityFromPDM extends BaseDirectoryAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            DataTransformEntityHandler.pdmFileToEntity(e);
        } catch (Exception exception) {
            DJJMessage.errorDialog(exception);
        }
    }

    @Override
    protected String getDirectoryName() {
        return "entity";
    }
}

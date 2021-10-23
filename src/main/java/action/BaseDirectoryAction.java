package main.java.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.jetbrains.annotations.NotNull;

/**
 * 基于文件夹判定的Action
 * 判断用户选择的包是否是指定包
 */
public abstract class BaseDirectoryAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabledAndVisible(false);
        VirtualFile data = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (data == null) {
            return;
        }
        PsiDirectory directory = PsiDirectoryFactory.getInstance(e.getProject())
                .createDirectory(data);
        //判断文件夹名称是否是 entity
        if (this.getDirectoryName().equals(directory.getName())) {
            e.getPresentation().setEnabledAndVisible(true);
        }
    }

    protected abstract String getDirectoryName();
}

package main.java.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import main.java.common.DJJMessage;
import main.java.common.UseNeedImportClass;
import main.java.transform.ServiceTransformHandler;

/**
 * service 转换为控制器
 */
public class ServiceToController extends BaseJavaFileAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            ServiceTransformHandler.serviceToController(e);
        } catch (Exception exception) {
            DJJMessage.errorDialog(exception);
        }
    }

    @Override
    public boolean checkEditor() {
        return false;
    }

    @Override
    public boolean judgePsiElement(PsiElement psiElement) {
        return true;
    }

    @Override
    public String getJudgeAnnotation() {
        return UseNeedImportClass.Service.getImportContent();
    }

    @Override
    public boolean ifJavaFileShowMenu() {
        return true;
    }
}

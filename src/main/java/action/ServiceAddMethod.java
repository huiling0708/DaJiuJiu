package main.java.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import main.java.common.DJJMessage;
import main.java.common.UseNeedImportClass;
import main.java.transform.ServiceMethodHandler;

/**
 * 为service 创建方法
 */
public class ServiceAddMethod extends BaseJavaFileAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            ServiceMethodHandler.createMethod(e);
        } catch (Exception exception) {
            DJJMessage.errorDialog(exception);
        }
    }

    @Override
    public boolean judgePsiElement(PsiElement psiElement) {
        return psiElement instanceof PsiClass;
    }

    @Override
    public String getJudgeAnnotation() {
        return UseNeedImportClass.Service.getImportContent();
    }
}

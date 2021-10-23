package main.java.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import main.java.common.DJJMessage;
import main.java.common.UseNeedImportClass;
import main.java.transform.ServiceTransformHandler;

/**
 * service 中的指定方法添加到指定控制器
 */
public class ServiceMethodAddToController extends BaseJavaFileAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            ServiceTransformHandler.serviceMethodTransform(e);
            DJJMessage.messageDialog("添加成功！");
        } catch (Exception exception) {
            DJJMessage.errorDialog(exception);
        }
    }

    @Override
    public boolean judgePsiElement(PsiElement psiElement) {
        return psiElement instanceof PsiMethod;
    }

    @Override
    public String getJudgeAnnotation() {
         return UseNeedImportClass.Service.getImportContent();
    }
}

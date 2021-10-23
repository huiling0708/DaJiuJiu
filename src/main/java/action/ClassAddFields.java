package main.java.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import main.java.common.DJJMessage;
import main.java.common.UseNeedImportClass;
import main.java.transform.AddFieldsHandler;

/**
 * 把指定类中勾选的字段 添加到 Action选择的类中
 */
public class ClassAddFields extends BaseJavaFileAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            AddFieldsHandler.handle(e);
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
        return UseNeedImportClass.ApiModel.getImportContent();
    }
}

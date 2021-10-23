package main.java.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import main.java.common.DJJMessage;
import main.java.common.UseNeedImportClass;
import main.java.transform.ChekEntityFieldHandler;

/**
 * 检查实体字段
 * 主要用于检查用户选择的字段所在实体类被哪些Vo类引用了
 * 同时检查被引用的实体类中，是否包含被选择的字段
 * 如果不包含，可以选择把选择的字段添加到Vo类中
 */
public class ChekEntityField extends BaseJavaFileAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            ChekEntityFieldHandler.handle(e);
        } catch (Exception exception) {
            DJJMessage.errorDialog(exception);
        }
    }

    @Override
    public String getJudgeAnnotation() {
        return UseNeedImportClass.Entity.getImportContent();
    }

    @Override
    public boolean judgePsiElement(PsiElement psiElement) {
        return psiElement instanceof PsiField;
    }
}

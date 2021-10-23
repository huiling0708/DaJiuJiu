package main.java.action;

import com.intellij.psi.PsiElement;
import main.java.common.UseNeedImportClass;

public abstract class BaseEntityFileAction extends BaseJavaFileAction {


    @Override
    public boolean judgePsiElement(PsiElement psiElement) {
        return true;
    }

    @Override
    public String getJudgeAnnotation() {
        return UseNeedImportClass.Entity.getImportContent();
    }

    @Override
    public boolean checkEditor() {
        return false;
    }

    @Override
    public boolean ifJavaFileShowMenu() {
        return true;
    }
}

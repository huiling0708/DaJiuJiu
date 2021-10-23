package main.java.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import main.java.common.DJJMessage;
import main.java.config.DJJConfigBean;
import main.java.config.DJJState;
import main.java.form.ValidateTypeAddForm;
import org.jetbrains.annotations.NotNull;

public class AddValidateTypeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        try {
            new ValidateTypeAddForm(anActionEvent.getProject());
        } catch (Exception exception) {
            DJJMessage.errorDialog(exception);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabledAndVisible(false);
        //未选择任何元素
        PsiElement psiElement = e.getData(PlatformDataKeys.PSI_ELEMENT);
        if (psiElement == null) {
            return;
        }
        //当前文件非java文件
        PsiFile containingFile = psiElement.getContainingFile();
        if (containingFile == null || !(containingFile instanceof PsiJavaFile)) {
            return;
        }
        //读取
        PsiClass psiClass = ((PsiJavaFile) containingFile).getClasses()[0];
        DJJConfigBean configBean = DJJState.getInstance(e.getProject()).getConfigBean();
        String validateTypeEnum = configBean.getValidateTypeEnum();
        if (validateTypeEnum == null) {
            return;
        }
        if (!validateTypeEnum.equals(psiClass.getQualifiedName())) {
            return;
        }
        e.getPresentation().setEnabledAndVisible(true);
    }
}

package main.java.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;

/**
 * java文件基础Action
 * 当 Action 基于某个文件处理时
 * 验证文件是否配置了指定注解
 * 验证当前用户选择的元素是否是指定元素
 */
public abstract class BaseJavaFileAction extends AnAction {

    protected final static String ELEMENT_METHOD_FLAG = "PsiMethod:";
    protected final static String ELEMENT_CLASS_FLAG = "PsiClass:";
    protected final static String ELEMENT_FIELDS_FLAG = "PsiField:";

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabledAndVisible(false);
        //是否检查编辑器 在用户打开编辑页面时才有值
        if (checkEditor()) {
            Editor editor = e.getData(PlatformDataKeys.EDITOR);
            if (editor == null) {
                return;
            }
        }
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
        //如果是java文件，是否显示菜单 仅显示，但并不可以点击
        if (ifJavaFileShowMenu()) {
            e.getPresentation().setVisible(true);
        }
        //判断用户选择的元素是否是指定元素
        if (!judgePsiElement(psiElement)) {
            return;
        }
        //验证是否标记了指定注解
        PsiJavaFile psiJavaFile = (PsiJavaFile) containingFile;
        PsiAnnotation ann = psiJavaFile.getClasses()[0]
                .getAnnotation(getJudgeAnnotation());
        if (ann == null) {
            return;
        }
        e.getPresentation().setEnabledAndVisible(true);
    }

    public boolean checkEditor() {
        return true;
    }

    //如果是java文件，是否显示菜单
    public boolean ifJavaFileShowMenu() {
        return false;
    }

    //判断PsiClass使用的注解注解
    public abstract String getJudgeAnnotation();

    //判断鼠标选中元素是否是指定元素
    public abstract boolean judgePsiElement(PsiElement psiElement);


}

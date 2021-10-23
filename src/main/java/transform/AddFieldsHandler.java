package main.java.transform;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.ClassUtil;
import main.java.bean.HandleNonEditableField;
import main.java.common.PsiHelper;
import main.java.form.ClassAddFieldForm;
import main.java.utils.FreemarkerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加字段处理器 把指定类中的指定字段 添加到 另一个指定的类中
 */
public abstract class AddFieldsHandler {

    public static void handle(AnActionEvent event) {
        //获取类名称
        PsiJavaFile javaFile = PsiHelper.getPsiJavaFile(event);
        PsiClass psiClass = javaFile.getClasses()[0];
        Project project = event.getProject();

        new ClassAddFieldForm(project, psiClass, handleFields -> {
            List<PsiClass> importClasses = new ArrayList<>();//需要导入的类型
            List<String> psiFieldJavaCode = new ArrayList<>();//生成的java 代码
            for (HandleNonEditableField handleField : handleFields) {
                if (!handleField.isSelected()) {
                    continue;
                }
                PsiType psiType = handleField.getPsiType();
                //判断生成的java 字段的类型，是否需要导入
                if (PsiHelper.needImport(psiType)) {
                    PsiManager psiManager = PsiManager.getInstance(project);
                    PsiClass importClass = ClassUtil.findPsiClass(psiManager,
                            psiType.getInternalCanonicalText());
                    importClasses.add(importClass);
                }
                //生成java 字段
                String javaCode = FreemarkerUtils.processJavaCode(project,
                        "property.ftl", handleField);
                psiFieldJavaCode.add(0, javaCode);//倒叙添加
            }


            WriteCommandAction.runWriteCommandAction(project, () -> {
                //最后一个字段
                PsiField[] fields = psiClass.getFields();
                PsiField lastField = fields.length == 0 ?
                        null : fields[fields.length - 1];
                //处理导入
                importClasses.forEach(i -> javaFile.importClass(i));
                PsiElement psiWhiteSpace = PsiHelper.getFieldPsiWhiteSpace(psiClass);
                //处理字段
                PsiElementFactory elementFactory = PsiElementFactory.getInstance(project);

                for (String code : psiFieldJavaCode) {
                    PsiField psiField = elementFactory.createFieldFromText(code, null);
                    if (lastField == null) {
                        psiClass.add(psiField);
                    } else {
                        psiClass.addAfter(psiField, lastField);//添加到该类的最后一个字段后面
                        if (psiWhiteSpace != null) {
                            psiClass.addAfter(psiWhiteSpace, lastField);
                        }
                    }
                }
            });
        });
    }
}

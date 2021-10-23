package main.java.transform;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.ClassUtil;
import main.java.bean.HandleClassField;
import main.java.bean.VoClassBean;
import main.java.bean.VoClassFormBean;
import main.java.common.PsiHelper;
import main.java.common.UseNeedImportClass;
import main.java.form.ChekEntityFieldForm;
import main.java.utils.FreemarkerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理选中的实体字段 在引用该实体的vo类中是否包含此字段 如果不包含是否添加到指定vo类中
 */
public abstract class ChekEntityFieldHandler {

    /**
     * 前置处理 用于Action 调用
     *
     * @param event
     */
    public static void handle(AnActionEvent event) {

        PsiElement psiElement = event.getData(PlatformDataKeys.PSI_ELEMENT);
        if (psiElement == null || !(psiElement instanceof PsiField)) {
            return;
        }
        //获取字段名称
        String fieldName = ((PsiField) psiElement).getName();
        if (fieldName == null) {
            return;
        }
        //获取当前实体类文件与实体类
        PsiJavaFile entityJavaFile = PsiHelper.getPsiJavaFile(event);
        PsiClass entityClass = entityJavaFile.getClasses()[0];

        //扫描实体包下的vo包，并获取包类所有 vo类
        PsiDirectory parent = entityJavaFile.getParent();
        PsiDirectory vo = parent.getParentDirectory().findSubdirectory("vo");
        if (vo == null) {
            throw new RuntimeException("该实体未创建相关查询Vo类");
        }
        List<VoClassBean> otherVoClasses = new ArrayList<>();
        List<VoClassBean> needAddFieldVoClasses = new ArrayList<>();
        PsiFile[] files = vo.getFiles();
        for (PsiFile file : files) {
            if (!(file instanceof PsiJavaFile)) {
                continue;
            }
            PsiJavaFile f = (PsiJavaFile) file;
            PsiClass voClass = f.getClasses()[0];
            //检查该vo类是否引用了本实体
            if (!checkVoClassRelationEntity(event.getProject(), voClass, entityClass)) {
                continue;
            }
            //是否包指定字段
            PsiField psiField = voClass.findFieldByName(fieldName, false);
            if (psiField == null) {
                needAddFieldVoClasses.add(new VoClassBean(voClass));
            } else {
                //添加引用了该实体的vo
                otherVoClasses.add(new VoClassBean(voClass));
            }
        }
        //获取字段
        PsiField entityField = entityClass.findFieldByName(fieldName, false);
        VoClassFormBean voClassFormBean =
                new VoClassFormBean(entityClass, entityField, otherVoClasses, needAddFieldVoClasses);
        new ChekEntityFieldForm(event.getProject(), voClassFormBean);
    }

    /**
     * 回调处理 用于窗体类在点击ok按钮后调用
     *
     * @param project
     * @param voClassFormBean
     */
    public static void callBackHandle(Project project, VoClassFormBean voClassFormBean) {
        PsiField psiField = voClassFormBean.getPsiField();
        HandleClassField handleClassField = new HandleClassField(
                voClassFormBean.getFieldName(), voClassFormBean.getFieldDescription(), psiField.getType()
        );
        //生成java 字段
        String javaCode = FreemarkerUtils.processJavaCode(project,
                "property.ftl", handleClassField);
        PsiClass importPsiClass = null;
        //判断生成的java 字段的类型，是否需要导入
        if (PsiHelper.needImport(psiField.getType())) {
            PsiManager psiManager = PsiManager.getInstance(project);
            importPsiClass = ClassUtil.findPsiClass(psiManager, psiField.
                    getType().getInternalCanonicalText());
        }
        //根据用户选择 把生成的java字段代码片段添加到选择的类中
        for (VoClassBean voClass : voClassFormBean.getNeedAddFieldVoClasses()) {
            if (!voClass.isSelected()) {
                continue;
            }
            PsiClass psiClass = voClass.getPsiClass();
            //处理导入
            if (importPsiClass != null) {
                PsiJavaFile containingFile = (PsiJavaFile) psiClass.getContainingFile();
                PsiClass finalImportPsiClass = importPsiClass;
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    containingFile.importClass(finalImportPsiClass);
                });
            }
            //处理字段
            PsiField[] fields = psiClass.getFields();
            PsiField lastField = fields[fields.length - 1];
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiElementFactory elementFactory = PsiElementFactory.getInstance(project);
                PsiElement psiWhiteSpace = PsiHelper.getFieldPsiWhiteSpace(psiClass);
                PsiField fieldFromText = elementFactory.createFieldFromText(javaCode, null);
                psiClass.addAfter(fieldFromText, lastField);//添加到该类的最后一个字段后面
                if (psiWhiteSpace != null) {
                    psiClass.addAfter(psiWhiteSpace, lastField);
                }
            });
        }
    }

    //检查该vo类是否引用了指定实体
    private static boolean checkVoClassRelationEntity(Project project, PsiClass voClass, PsiClass entityClass) {
        PsiAnnotation queryProvideAnn = voClass.
                getAnnotation(UseNeedImportClass.QueryProvide.getImportContent(project));
        if (queryProvideAnn == null) {
            return false;
        }
        PsiAnnotationMemberValue memberValue = queryProvideAnn.findAttributeValue("entityType");
        if (null == memberValue) {
            return false;
        }
        String text = memberValue.getText();
        int i = text.indexOf(".class");
        text = text.substring(0, i);
        return entityClass.getName().equals(text);
    }
}

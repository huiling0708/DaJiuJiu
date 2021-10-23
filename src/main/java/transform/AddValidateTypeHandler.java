package main.java.transform;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import main.java.bean.ValidateTypeBean;
import main.java.common.DJJHelper;
import main.java.common.UseNeedImportClass;
import main.java.utils.FreemarkerUtils;

import java.util.List;

/**
 * 添加验证类型处理器
 */
public class AddValidateTypeHandler {

    public static void addValidateType(Project project, ValidateTypeBean typeBean) {
        //读取ValidateType
        PsiClass validateTypeClass = JavaPsiFacade.getInstance(project).findClass(
                UseNeedImportClass.ValidateType.getImportContent(project),
                GlobalSearchScope.projectScope(project));

        PsiJavaFile validateTypeJavaFile = (PsiJavaFile) validateTypeClass.getContainingFile();

        PsiClass entityClass = typeBean.getEntityClass();
        if (entityClass == null) {
            throw new RuntimeException("未选择实体类");
        }
        if (DJJHelper.isBlank(typeBean.getTypeName())) {
            throw new RuntimeException("未输入验证类型名称");
        }
        for (PsiField field : validateTypeClass.getFields()) {
            if (field.getName().equals(typeBean.getTypeName())) {
                throw new RuntimeException(String.format("验证类型[%s]已存在", typeBean.getTypeName()));
            }
        }

        if (DJJHelper.isBlank(typeBean.getTypeDescribe())) {
            throw new RuntimeException("未输入验证类型描述");
        }
        if (DJJHelper.isBlank(typeBean.getFieldName())) {
            throw new RuntimeException("未选择实体字段");
        }
        //生成java代码
        String javaCode = FreemarkerUtils.processJavaCode(project,
                "addValidateType.ftl", typeBean);


        WriteCommandAction.runWriteCommandAction(project, () -> {
            //导入实体类
            validateTypeJavaFile.importClass(entityClass);

            PsiField[] fields = validateTypeClass.getFields();
            PsiField lastEnumConstant = null;//读取最后一个枚举值
            for (PsiField psiField : fields) {
                if (psiField instanceof PsiEnumConstant) {
                    lastEnumConstant = psiField;
                }
            }

            List<PsiElement> elements =
                    PsiTreeUtil.getElementsOfRange(fields[0], fields[fields.length - 1]);
            PsiJavaToken semicolonToken = null;//分号元素
            PsiJavaToken commaToken = null;//逗号元素
            PsiElement psiWhiteSpace = null;//换行空格元素
            for (PsiElement element : elements) {
                if (psiWhiteSpace == null && element instanceof PsiWhiteSpace) {
                    psiWhiteSpace = element.copy();
                    continue;
                }
                if (element instanceof PsiJavaToken) {
                    PsiJavaToken t = (PsiJavaToken) element;
                    if (commaToken == null && t.getText().equals(",")) {
                        commaToken = (PsiJavaToken) element.copy();
                        continue;
                    }
                    if (semicolonToken == null && t.getText().equals(";")) {
                        semicolonToken = t;
                        break;
                    }
                }
            }

            //处理字段
            PsiElementFactory elementFactory = PsiElementFactory.getInstance(project);
            PsiEnumConstant text = elementFactory.createEnumConstantFromText(javaCode, null);
            validateTypeClass.add(text);
            validateTypeClass.addAfter(psiWhiteSpace, lastEnumConstant);
            validateTypeClass.addBefore(psiWhiteSpace, semicolonToken);
            validateTypeClass.addBefore(commaToken, semicolonToken);

        });
    }
}

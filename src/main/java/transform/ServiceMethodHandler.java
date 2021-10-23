package main.java.transform;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.ClassUtil;
import main.java.bean.HandleNonEditableField;
import main.java.bean.HandleServiceMethod;
import main.java.common.DJJHelper;
import main.java.common.MethodHandleType;
import main.java.common.PsiHelper;
import main.java.common.UseNeedImportClass;
import main.java.form.ServiceMethodForm;
import main.java.utils.FreemarkerUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service 添加方法处理类
 */
public abstract class ServiceMethodHandler {

    /**
     * 创建方法
     *
     * @param event
     */
    public static void createMethod(AnActionEvent event) {
        PsiElement psiElement = event.getData(PlatformDataKeys.PSI_ELEMENT);
        if (psiElement == null||!(psiElement instanceof PsiClass)) {
            return;
        }
        //获取类名称
        String serviceClassName = ((PsiClass) psiElement).getName();
        if (serviceClassName == null) {
            return;
        }
        PsiJavaFile serviceJavaFile = PsiHelper.getPsiJavaFile(event);
        PsiClass servicePsiClass = serviceJavaFile.getClasses()[0];

        //获取实体类名称
        String entityClassName = serviceClassName.replace("Service", "");

        //期望自动找到关联的实体类，在当前包下寻找 entity包
        // 并通过服务名称 去掉Service后缀得到 实体类名 如果存在，则使用
        // 不匹配包含关系，如果不存在，则交由用户自行选择
        PsiDirectory containingDirectory = serviceJavaFile.getContainingDirectory();
        PsiDirectory entityDirectory = containingDirectory.findSubdirectory("entity");
        PsiFile file = entityDirectory.findFile(entityClassName + ".java");
        PsiClass entityClass = null;
        if (file != null) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) file;
            entityClass = psiJavaFile.getClasses()[0];
        }

        //展示Service添加方法窗体
        new ServiceMethodForm(event.getProject(), servicePsiClass, entityClass);
    }

    /**
     * 创建 方法java 代码块 并添加到该Service中
     *
     * @param project
     * @param serviceClass
     * @param methodBean
     */
    public static void createMethodJavaCode(Project project, PsiClass serviceClass, HandleServiceMethod methodBean) {
        if (methodBean == null) {
            return;
        }
        //判断方法名
        PsiMethod[] methodsByName = serviceClass.findMethodsByName(methodBean.getMethodName(), false);
        if (methodsByName != null && methodsByName.length > 0) {
            throw new RuntimeException("方法[" + methodBean.getMethodName() + "]已存在!");
        }
        //处理导入
        methodBean.addImport(methodBean.getRelationEntity());
        methodBean.addImport(methodBean.getInputParam());
        methodBean.addImport(methodBean.getOutputParam());

        //事务注解导入
        methodBean.addImport(UseNeedImportClass.Transactional.getImportContent());

        //如果存在入参
        //把参数中的所有字段存入set 便于后续生成方法时使用
        String inputParam = methodBean.getInputParam();
        Set<String> paramFieldsNameSet = new HashSet<>();
        if (!DJJHelper.isBlank(methodBean.getInputParam())) {
            PsiManager psiManager = PsiManager.getInstance(project);
            PsiClass psiClass = ClassUtil.findPsiClass(psiManager, inputParam);
            for (PsiField field : psiClass.getFields()) {
                paramFieldsNameSet.add(field.getName());
            }
        }
        //根据方法类型 处理字段
        if (MethodHandleType.DELETE.equals(methodBean.getHandleType())
                || MethodHandleType.UPDATE.equals(methodBean.getHandleType())) {
            methodBean.addImport(UseNeedImportClass.JpaWrapper.getImportContent(project));
            handleFields("条件", methodBean, methodBean.getConditionFields(), paramFieldsNameSet);
            if (MethodHandleType.UPDATE.equals(methodBean.getHandleType())) {
                handleFields("更新", methodBean, methodBean.getUpdateFields(), paramFieldsNameSet);
            }
        } else {
            if (!DJJHelper.isBlank(methodBean.getInputParam())) {
                methodBean.addImport(UseNeedImportClass.CopyPropertyUtils.getImportContent(project));
            }
            handleNeedSetFields(project, methodBean, paramFieldsNameSet);
        }


        WriteCommandAction.runWriteCommandAction(project, () -> {
            //获取service文件添加导入
            PsiJavaFile psiJavaFile = (PsiJavaFile) serviceClass.getContainingFile();
            PsiManager psiManager = PsiManager.getInstance(project);
            for (String s : methodBean.getImportContent()) {
                PsiClass psiClass = ClassUtil.findPsiClass(psiManager, s);
                psiJavaFile.importClass(psiClass);
            }

            //方法类容
            String javaCode = FreemarkerUtils.processJavaCode(project,
                    methodBean.getHandleType().getTemplateName(), methodBean);
            PsiElementFactory elementFactory = PsiElementFactory.getInstance(project);
            PsiMethod createPsiMethod = elementFactory.createMethodFromText(javaCode, null);
            serviceClass.add(createPsiMethod);
        });
    }

    //保存方法时，处理需要set 的字段
    private static void handleNeedSetFields(Project project, HandleServiceMethod methodBean, Set<String> paramFieldsNameSet) {

        String relationEntity = methodBean.getRelationEntity();
        PsiManager psiManager = PsiManager.getInstance(project);
        PsiClass psiClass = ClassUtil.findPsiClass(psiManager, relationEntity);
        List<HandleNonEditableField> methodFields = new ArrayList<>();
        for (PsiField field : psiClass.getFields()) {
            if (paramFieldsNameSet.contains(field.getName())) {
                continue;
            }
            //判断是否是主键且有主键生成策略，如果是则不需要set
            if (checkFieldIsPk(field)) {
                continue;
            }

            HandleNonEditableField methodField = new HandleNonEditableField(field);
            if (methodField.getDescription() == null) {
                continue;
            }
            methodFields.add(methodField);
        }
        methodBean.setSaveNeedSetValueFields(methodFields);
    }

    /**
     * 检查字段是否是主键且有主键生成策略
     *
     * @return
     */
    private static boolean checkFieldIsPk(PsiField field) {
        if (field.getAnnotation(UseNeedImportClass.Id.getImportContent()) == null) {
            return false;
        }
        return field.getAnnotation(UseNeedImportClass.GeneratedValue.getImportContent()) != null;
    }

    /**
     * 处理字段
     *
     * @param name
     * @param methodBean
     * @param fieldList
     * @param paramFieldsNameSet
     */
    private static void handleFields(
            String name, HandleServiceMethod methodBean,
            List<HandleNonEditableField> fieldList, Set<String> paramFieldsNameSet) {
        int count = 0;
        for (HandleNonEditableField methodField : fieldList) {
            if (!methodField.isSelected()) {
                continue;
            }
            count++;
            //是否需要导入
            boolean b = PsiHelper.needImport(methodField.getPsiType());
            if (b) {
                methodBean.addImport(methodField.getPsiType().getInternalCanonicalText());
            }
            //指定字段在 入参中是否能被找到
            if (paramFieldsNameSet.contains(methodField.getName())) {
                methodField.setExistsParam(true);
            }
        }
        if (count == 0) {
            throw new RuntimeException(String.format("[%s]方法至少存在一个[%s]字段",
                    methodBean.getHandleType().getDescription(), name));
        }
    }
}

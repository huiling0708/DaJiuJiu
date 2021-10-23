package main.java.transform;

import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.ide.util.TreeJavaClassChooserDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.ClassUtil;
import com.intellij.psi.util.PsiUtil;
import main.java.bean.AutowiredParamBean;
import main.java.bean.HandleControllerClass;
import main.java.bean.HandleControllerMethod;
import main.java.common.*;
import main.java.component.ServiceTransformTable;
import main.java.config.DJJConfigBean;
import main.java.config.DJJState;
import main.java.form.BuildClassForm;
import main.java.form.UIFieldTable;
import main.java.utils.FreemarkerUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service服务转换类
 */
public abstract class ServiceTransformHandler {

    /**
     * Service 转换为 控制器
     *
     * @param event
     */
    public static void serviceToController(AnActionEvent event) {
        PsiJavaFile javaFile = PsiHelper.getPsiJavaFile(event);
        if (javaFile == null) {
            return;
        }

        //获取第一个class文件
        PsiClass serviceClass = javaFile.getClasses()[0];

        //是否配置@Service 标签
        PsiAnnotation entityAnn = serviceClass.getAnnotation(UseNeedImportClass.Service.getImportContent());
        if (entityAnn == null) {
            throw new RuntimeException("仅支持Service类的转换");
        }
        HandleControllerClass handleClass = new HandleControllerClass();
        //元名
        String name = serviceClass.getName();
        if (name.endsWith("Service")) {
            name = name.replace("Service", "");
        }
        handleClass.setClassBeanName(name);
        //描述
        String docCommentText = PsiHelper.getDocCommentText(serviceClass.getDocComment());
        handleClass.setDescription(docCommentText);

        List<HandleControllerMethod> handleMethods = new ArrayList<>();//方法
        Set<String> importContent = new HashSet<>();//导入
        importContent.add(serviceClass.getQualifiedName());


        for (PsiMethod method : serviceClass.getMethods()) {
            //只读取公共方法
            if (PsiUtil.ACCESS_LEVEL_PUBLIC != PsiUtil.getAccessLevel(method.getModifierList())) {
                continue;
            }
            HandleControllerMethod handleMethod = psiMethodToHandleMethod(event.getProject(), method, importContent);
            handleMethods.add(handleMethod);
        }

        handleClass.setMethods(handleMethods);
        handleClass.setImportContent(importContent);

        //表单通用处理
        IClassForm iClassForm = new IClassForm() {
            @Override
            public String getTitle() {
                return "Create Controller Class";
            }

            @Override
            public String getClassName() {
                return handleClass.getControllerName();
            }

            @Override
            public String getClassDescription() {
                return handleClass.getDescription();
            }
        };

        //成功处理
        IButtonEvent okButtonEvent = new IButtonEvent() {
            @Override
            public boolean handle(MouseEvent e, JFrame jFrame, JTextField classNameText, JTextField descriptionText) {
                jFrame.setVisible(false);
                handleClass.setControllerName(classNameText.getText());
                handleClass.setDescription(descriptionText.getText());
                //指定生成的控制器存放的包
                PackageChooserDialog selector = new PackageChooserDialog(
                        "请选择一个存放Controller的包", event.getProject());
                selector.selectPackage(javaFile.getPackageName());
                selector.show();
                PsiPackage selectedPackage = selector.getSelectedPackage();
                if (selectedPackage == null) {
                    DJJMessage.errorDialog("未选择任何包");
                    jFrame.setVisible(true);
                    return false;
                }
                PsiDirectory directory = selectedPackage.getDirectories()[0];
                boolean b = PsiHelper.checkClassName(directory, handleClass.getControllerName());
                if (!b) {
                    jFrame.setVisible(true);
                    return false;
                }
                //创建java 文件
                PsiHelper.createJavaFile(event.getProject(), "serviceToController.ftl",
                        handleClass, directory);
                return true;
            }
        };

        //方法详情按钮
        IButtonEvent fieldsButtonEvent = new IButtonEvent() {
            @Override
            public void handle(MouseEvent e) {
                new UIFieldTable(new TableDataHandle<>(
                        HandleControllerMethod.class, handleClass.getMethods()),
                        new Dimension(800, 300), "方法详情",
                        h -> new ServiceTransformTable(h));
            }

            @Override
            public String buttonName() {
                return "Methods";
            }
        };

        //展示 java class类窗体
        new BuildClassForm(iClassForm, okButtonEvent, fieldsButtonEvent);
    }

    /**
     * 把service中的方法添加到控制器中
     *
     * @param event
     */
    public static void serviceMethodTransform(AnActionEvent event) {
        PsiElement psiElement = event.getData(PlatformDataKeys.PSI_ELEMENT);
        if (psiElement == null||!(psiElement instanceof PsiMethod)) {
            return;
        }
        //获取方法名称
        String methodName = ((PsiMethod) psiElement).getName();//获取到方法名

        PsiJavaFile serviceJavaFile = PsiHelper.getPsiJavaFile(event);
        PsiClass servicePsiClass = serviceJavaFile.getClasses()[0];

        PsiMethod[] methods = servicePsiClass.getMethods();
        PsiMethod psiMethod = null;
        for (PsiMethod method : methods) {
            if (method.getName().equals(methodName)) {
                psiMethod = method;
                break;
            }
        }
        if (psiMethod == null) {
            throw new RuntimeException("无法读取" + methodName + "方法");
        }

        //方法处理
        if (PsiUtil.ACCESS_LEVEL_PUBLIC != PsiUtil.getAccessLevel(psiMethod.getModifierList())) {
            throw new RuntimeException(methodName + "方法不是一个public方法");
        }
        Set<String> importContent = new HashSet<>();//导入

        HandleControllerMethod handleMethod = psiMethodToHandleMethod(event.getProject(), psiMethod, importContent);
        handleMethod.setServiceParam(DJJHelper.firstLowerCase(servicePsiClass.getName()));
        handleMethod.setServiceMethodName(psiMethod.getName());

        //指定控制器
        TreeJavaClassChooserDialog selector = new TreeJavaClassChooserDialog(
                "请选择要把方法添加到哪个Controller中", event.getProject(), servicePsiClass);
        selector.show();
        PsiClass controllerPsiClass = selector.getSelected();
        if (controllerPsiClass == null) {
            throw new RuntimeException("未选择控制器");
        }
        //判断是否是一个控制器类
        PsiAnnotation restControllerAnn =
                controllerPsiClass.getAnnotation(UseNeedImportClass.RestController.getImportContent());
        if (restControllerAnn == null) {
            PsiAnnotation controllerAnn = controllerPsiClass.getAnnotation(UseNeedImportClass.Controller.getImportContent());
            if (controllerAnn == null) {
                throw new RuntimeException("选择的类不是一个Controller类");
            }
        }

        //检查方法名称
        boolean b = checkMethodName(controllerPsiClass, handleMethod);
        if (!b) {
            return;
        }

        //是否还需要注入 查询控制器中 是否有当前service类型的字段
        PsiField[] fields = controllerPsiClass.getFields();
        boolean needAddService = true;
        for (PsiField psiField : fields) {
            if (servicePsiClass.getQualifiedName()
                    .equals(psiField.getType().getInternalCanonicalText())) {
                needAddService = false;
                break;
            }
        }

        //添加注入
        if (needAddService) {

            //如果需要添加注入，则表示原本控制器可能没有其它方法，则导入以下
            importContent.add(UseNeedImportClass.Autowired.getImportContent());
            importContent.add(UseNeedImportClass.ApiOperation.getImportContent());
            importContent.add(UseNeedImportClass.PostMapping.getImportContent());
            importContent.add(UseNeedImportClass.RequestBody.getImportContent());
            importContent.add(UseNeedImportClass.RequestBody.getImportContent());
            //控制器返回
            importContent.add(UseNeedImportClass.ControllerResultVo.getImportContent(event.getProject()));
            //服务类本身
            importContent.add(servicePsiClass.getQualifiedName());
            //创建注入 注入字段添加到控制器类中
            WriteCommandAction.runWriteCommandAction(event.getProject(), () -> {
                String javaCode = FreemarkerUtils.processJavaCode(event.getProject(),
                        "serviceAutowired.ftl",
                        new AutowiredParamBean(servicePsiClass.getName()));
                PsiElementFactory elementFactory = PsiElementFactory.getInstance(event.getProject());
                PsiField fieldFromText = elementFactory.createFieldFromText(javaCode, null);
                controllerPsiClass.add(fieldFromText);
            });
        }

        WriteCommandAction.runWriteCommandAction(event.getProject(), () -> {
            //获取控制器文件添加导入
            PsiJavaFile controllerJavaFile = (PsiJavaFile) controllerPsiClass.getContainingFile();
            PsiManager psiManager = PsiManager.getInstance(event.getProject());
            for (String s : importContent) {
                PsiClass psiClass = ClassUtil.findPsiClass(psiManager, s);
                controllerJavaFile.importClass(psiClass);
            }
            //方法类容
            String javaCode = FreemarkerUtils.processJavaCode(event.getProject(),
                    "serviceMethodTransform.ftl", handleMethod);
            PsiElementFactory elementFactory = PsiElementFactory.getInstance(event.getProject());
            PsiMethod createPsiMethod = elementFactory.createMethodFromText(javaCode, null);
            controllerPsiClass.add(createPsiMethod);
        });
    }

    /**
     * PsiMethod类 转换为 HandleMethod处理类
     *
     * @param project
     * @param psiMethod
     * @param importContent
     * @return
     */
    private static HandleControllerMethod psiMethodToHandleMethod(Project project, PsiMethod psiMethod, Set<String> importContent) {
        HandleControllerMethod handleMethod = new HandleControllerMethod();
        handleMethod.setName(psiMethod.getName());
        handleMethod.setDescription(PsiHelper.getDocCommentText(psiMethod.getDocComment()));

        //入参处理
        PsiParameterList parameterList = psiMethod.getParameterList();
        if (parameterList.getParametersCount() > 1) {
            throw new RuntimeException(psiMethod.getName() + "方法中参数个数大于1个,请把参数封装成一个class类");
        } else if (parameterList.getParametersCount() == 0) {
            handleMethod.setParamType(null);
        } else {
            PsiType type = parameterList.getParameter(0).getType();
            handleMethod.setParamType(type.getPresentableText());
            //如果需要导入的类，则是一个普通的java 基础类 则封装为单个参数类
            //否则是一个对象类
            if (PsiHelper.needImport(type)) {
                DJJHelper.addImport(type.getInternalCanonicalText(), importContent);
                //如果是一个List 则同样把该类封装为单个参数类 SingleParam
                if (type.getInternalCanonicalText().startsWith(
                        UseNeedImportClass.List.getImportContent())) {
                    handleMethod.setSingleParam(true);
                    DJJConfigBean configBean = DJJState.getInstance(project).getConfigBean();
                    importContent.add(configBean.getSingleParamClass());
                }
            } else {
                //如果是基本类型，则需要把类型展示为他的包装类型
                String typeText = handleMethod.getParamType();
                if (TypeMapping.basicJavaType(typeText)) {
                    String javaPackingType = TypeMapping.getTypeMappingByJavaType(typeText).
                            getJavaPackingType();
                    typeText = javaPackingType == null ? typeText : javaPackingType;
                    handleMethod.setParamType(typeText);
                }
                handleMethod.setSingleParam(true);
                DJJConfigBean configBean = DJJState.getInstance(project).getConfigBean();
                importContent.add(configBean.getSingleParamClass());
            }
        }

        //出参处理
        PsiType returnType = psiMethod.getReturnType();
        if (returnType.getPresentableText().equals("void")) {
            handleMethod.setReturnType(null);
        } else {
            String typeText = returnType.getPresentableText();
            //如果是基本类型，则需要把类型展示为他的包装类型
            if (TypeMapping.basicJavaType(typeText)) {
                String javaPackingType = TypeMapping.getTypeMappingByJavaType(typeText).
                        getJavaPackingType();
                typeText = javaPackingType == null ? typeText : javaPackingType;
            }
            handleMethod.setReturnType(typeText);
            if (PsiHelper.needImport(returnType)) {
                DJJHelper.addImport(returnType.getInternalCanonicalText(), importContent);
            }
        }
        handleMethod.setMappingValue(DJJHelper.buildMappingValue(handleMethod.getName()));

        return handleMethod;
    }

    /**
     * 检查方法名 同时检查控制器MappingValue
     *
     * @param controllerPsiClass
     * @param handleMethod
     * @return
     */
    private static boolean checkMethodName(PsiClass controllerPsiClass, HandleControllerMethod handleMethod) {
        Set<String> methodNameSet = new HashSet<>();
        Set<String> mappingValueSet = new HashSet<>();
        //控制器原有方法名 和 Mapping值
        for (PsiMethod method : controllerPsiClass.getMethods()) {
            methodNameSet.add(method.getName());

            PsiAnnotation annotation = method.getAnnotation(UseNeedImportClass.PostMapping.getImportContent());
            if (annotation == null) {
                continue;
            }
            PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
            if (value == null) {
                continue;
            }
            mappingValueSet.add(value.getText().replace("\"", ""));
        }
        //检查方法名
        String name = checkValue(methodNameSet, handleMethod.getName(), "方法名");
        if (DJJHelper.isBlank(name)) {
            return false;
        }
        //检查Mapping值
        handleMethod.setName(name);
        String mappingValue = checkValue(mappingValueSet, handleMethod.getMappingValue(), "Mapping值");
        if (DJJHelper.isBlank(mappingValue)) {
            return false;
        }
        handleMethod.setMappingValue(mappingValue);
        return true;
    }

    /**
     * 如果重复 要求用户重新输入
     *
     * @param set
     * @param value
     * @param message
     * @return
     */
    private static String checkValue(Set<String> set, String value, String message) {
        if (!set.contains(value)) {
            return value;
        }
        value = DJJMessage.inputDialog(message + "[" + value + "]已存在，请输入新的" + message, value);
        return checkValue(set, value, message);
    }
}

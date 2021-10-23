package main.java.transform;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import main.java.bean.*;
import main.java.common.*;
import main.java.component.EntityToParamTable;
import main.java.component.EntityToVoTable;
import main.java.config.DJJConfigBean;
import main.java.config.DJJState;
import main.java.form.BuildClassForm;
import main.java.form.UIFieldTable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 实体类转换处理器
 */
public abstract class EntityTransformHandler {

    /**
     * 转换为vo类
     *
     * @param event
     */
    public static void entityToVo(AnActionEvent event) {
        entityTransform(event.getProject(), PsiHelper.getPsiJavaFile(event),
                new ITransformParamOrVo() {
                    @Override
                    public String modelType() {
                        return "vo";
                    }

                    @Override
                    public void customHandleClass(PsiClass entityClass, HandleClass handleClass) {
                        String description = handleClass.getDescription();
                        handleClass.setDescription(description + "视图模型");
                        handleClass.setQueryName(description + "分页查询");
                        handleClass.setEntityClassName(entityClass.getName());
                        handleClass.setEntityClassNameParam(DJJHelper.firstLowerCase(entityClass.getName()));
                        handleClass.setSerialVersionUIDValue(DJJHelper.buildSerialVersionUID());
                    }

                    @Override
                    public void importHandle(Set<String> importContent, PsiJavaFile javaFile, PsiClass entityClass) {
                        //把实体类包先放入
                        importContent.add(entityClass.getQualifiedName());
                    }

                    @Override
                    public void createUIFieldTable(HandleClass handleClass) {
                        new UIFieldTable(new TableDataHandle<>(
                                HandleClassField.class, handleClass.getFields()), h ->
                                new EntityToVoTable(h));
                    }

                    @Override
                    public String templateName() {
                        return "entityToVo.ftl";
                    }

                });
    }

    /**
     * 转为param参数类
     *
     * @param event
     */
    public static void entityToParam(AnActionEvent event) {
        entityToParam(event.getProject(), PsiHelper.getPsiJavaFile(event),
                null, null, true, null);
    }

    /**
     * 转为param参数类
     *
     * @param project
     * @param psiJavaFile
     */
    public static void entityToParam(Project project, PsiJavaFile psiJavaFile,
                                     String paramName, String paramDescription,
                                     boolean showMessage,
                                     Consumer<String> consumer) {
        entityTransform(project, psiJavaFile, new ITransformParamOrVo() {
            @Override
            public String modelType() {
                return "param";
            }

            @Override
            public boolean showMessage() {
                return showMessage;
            }

            @Override
            public void customHandleClass(PsiClass entityClass, HandleClass handleClass) {
                if (!DJJHelper.isBlank(paramName)) {
                    handleClass.setClassName(DJJHelper.firstToUpperCase(paramName) + "Param");
                }
                String description = handleClass.getDescription();
                if (!DJJHelper.isBlank(paramDescription)) {
                    description = paramDescription;
                }
                handleClass.setDescription(description + "参数");
            }

            @Override
            public void handleAnnotationContent(PsiField psiField, HandleClassField handleClassField) {
                //先判断原实体列注解是否不允许为空
                PsiAnnotation columnAnn = psiField.
                        getAnnotation(UseNeedImportClass.Column.getImportContent());
                if (columnAnn == null) {
                    return;
                }
                PsiAnnotationMemberValue annValue = columnAnn.findAttributeValue("nullable");
                if (annValue == null || annValue.getText() == null) {
                    return;
                }
                Boolean nullable = Boolean.valueOf(annValue.getText());
                if (nullable == null || nullable) {
                    return;
                }
                handleClassField.setNotNull(true);
                UseNeedImportClass annType = TypeMapping
                        .getValidationAnnotationByType(handleClassField.getType());
                String annotationContent = annType == null ? "" : annType.name();
                handleClassField.setAnnotationContentAndBean(annotationContent);
            }

            @Override
            public void createUIFieldTable(HandleClass handleClass) {
                PsiClass entityClass = psiJavaFile.getClasses()[0];
                new UIFieldTable(new TableDataHandle<>(
                        HandleClassField.class, handleClass.getFields()), h ->
                        new EntityToParamTable(h).setProjectAndEntityClass(project, entityClass));
            }

            @Override
            public String templateName() {
                return "entityToParam.ftl";
            }

            @Override
            public Consumer<String> buildJavaClassQualifiedName() {
                return consumer;
            }
        });
    }

    /**
     * 转为 Service类
     *
     * @param event
     */
    public static void entityToService(AnActionEvent event) {
        entityTransform(event.getProject(), PsiHelper.getPsiJavaFile(event),
                new ITransform<HandleServiceClass>() {
                    @Override
                    public HandleServiceClass newHandleClass(String className, String classDescription) {
                        return new HandleServiceClass();
                    }

                    @Override
                    public String modelType() {
                        return "service";
                    }

                    @Override
                    public void customHandleClass(PsiClass entityClass, HandleServiceClass handleClass) {
                        handleClass.setDescription(handleClass.getDescription() + "服务");
                    }

                    @Override
                    public void createUIFieldTable(HandleServiceClass handleClass) {
                        DJJMessage.errorDialog("方法详情功能尚未开放!");
                    }

                    @Override
                    public String templateName() {
                        return "entityToService.ftl";
                    }
                });
    }

    /**
     * 实体类转换处理
     *
     * @param project
     * @param javaFile
     * @param transform
     * @param <T>
     */
    private static <T extends IHandleClass> void entityTransform(Project project, PsiJavaFile javaFile, ITransform<T> transform) {
        if (javaFile == null) {
            return;
        }
        //实体类仅仅包含一个class文件
        PsiClass entityClass = javaFile.getClasses()[0];
        //实体描述
        String classDescription = PsiHelper.getEntityDescription(entityClass);

        //className
        String className = entityClass.getName() + DJJHelper.firstToUpperCase(transform.modelType());

        //自定义处理
        T handleClass = transform.newHandleClass(className, classDescription);
        handleClass.setClassName(className);
        handleClass.setDescription(classDescription);
        transform.customHandleClass(entityClass, handleClass);

        //导入类容
        Set<String> importContent = new HashSet<>();
        transform.importHandle(importContent, javaFile, entityClass);
        //字段处理
        if (transform instanceof ITransformParamOrVo) {
            ITransformParamOrVo t = (ITransformParamOrVo) transform;
            t.fieldsHandle(entityClass, importContent, (HandleClass) handleClass);
        }
        handleClass.setImportContent(importContent);

        //表单通用处理
        IClassForm iClassForm = new IClassForm() {
            @Override
            public String getTitle() {
                return "Create " + DJJHelper.firstToUpperCase(transform.modelType())
                        + " Class";
            }

            @Override
            public String getClassName() {
                return handleClass.getClassName();
            }

            @Override
            public String getClassDescription() {
                return handleClass.getDescription();
            }

            @Override
            public boolean showMessage() {
                return transform.showMessage();
            }
        };

        //成功处理
        IButtonEvent okButtonEvent = new IButtonEvent() {
            @Override
            public boolean handle(MouseEvent e, JFrame jFrame, JTextField classNameText, JTextField descriptionText) {
                handleClass.setClassName(classNameText.getText());
                handleClass.setDescription(descriptionText.getText());

                //在实体包父级包下 查找指定包，如不存在，则创建
                PsiDirectory containingDirectory = javaFile.getContainingDirectory();
                PsiDirectory parentDirectory = containingDirectory.getParentDirectory();
                PsiDirectory childDirectory;
                if (transform instanceof ITransformParamOrVo) {
                    childDirectory = parentDirectory.findSubdirectory(transform.modelType());
                } else {
                    childDirectory = parentDirectory;//如果是服务，则直接放在实体包父级包下
                }
                //如果子包不存在，则创建子包，否则验证类名是否存在
                String childDirectoryName = null;
                if (childDirectory == null) {
                    childDirectoryName = transform.modelType();
                } else {
                    boolean b = PsiHelper.checkClassName(childDirectory, handleClass.getClassName());
                    if (!b) {
                        return false;
                    }
                    parentDirectory = childDirectory;
                }
                //注解导入处理
                if (transform instanceof ITransformParamOrVo) {
                    String descriptionValue = descriptionText.getText();
                    if (!descriptionValue.contains("查询")) {
                        descriptionValue = descriptionValue.replace("视图模型", "");
                        descriptionValue += "分页查询";
                    }
                    HandleClass h = (HandleClass) handleClass;
                    h.setQueryName(descriptionValue);
                    ITransformParamOrVo t = (ITransformParamOrVo) transform;
                    t.annotationImportHandle(project, h, importContent);
                }

                Consumer<String> consumer = transform.buildJavaClassQualifiedName();
                if (consumer != null) {
                    String paramPackageName = javaFile.getPackageName().replace("entity", "param");
                    String qualifiedName = paramPackageName + "." + handleClass.getClassName();
                    consumer.accept(qualifiedName);
                }

                //创建java 文件
                PsiHelper.createJavaFile(project, transform.templateName(),
                        handleClass, parentDirectory, childDirectoryName);
                return true;
            }
        };

        IButtonEvent fieldsButtonEvent = new IButtonEvent() {
            @Override
            public void handle(MouseEvent e) {
                transform.createUIFieldTable(handleClass);
            }

            @Override
            public String buttonName() {
                return transform instanceof ITransformParamOrVo ? "Fields" : "Methods";
            }
        };

        new BuildClassForm(iClassForm, okButtonEvent, fieldsButtonEvent);
    }

    /**
     * 参数与Vo转换接口
     */
    private interface ITransformParamOrVo extends ITransform<HandleClass> {
        @Override
        default HandleClass newHandleClass(String className, String classDescription) {
            return new HandleClass();
        }

        //字段处理
        default void fieldsHandle(PsiClass entityClass, Set<String> importContent, HandleClass handleClass) {
            //字段处理
            List<HandleClassField> fields = new ArrayList<>();
            PsiField[] allFields = entityClass.getFields();
            for (PsiField psiField : allFields) {
                String fieldDescription = PsiHelper.getEntityFieldDescription(psiField);
                if (fieldDescription == null) {
                    continue;
                }
                PsiType type = psiField.getType();
                HandleClassField handleClassField = new HandleClassField(psiField.getName(), fieldDescription, type);
                this.handleAnnotationContent(psiField, handleClassField);
                //加入字段中
                fields.add(handleClassField);
            }
            handleClass.setFields(fields);
        }

        //注解导入处理
        default void annotationImportHandle(Project project, HandleClass handleClass, Set<String> importContent) {
            handleClass.getFields().forEach(x -> {
                JFrame paramFrame = x.getParamFrame();
                //如果存在参数窗体资源则释放
                if (paramFrame != null) {
                    paramFrame.dispose();
                    x.setParamFrame(null);
                }
                //被选中才处理导入
                if (!x.isSelected()) {
                    return;
                }
                //处理注解导入 param 时是验证注解 vo时是QueryField注解
                List<ValidateAnnotationBean> annotationBeanList = x.getAnnotationBeanList();
                if (annotationBeanList != null && annotationBeanList.size() > 0) {
                    //如果存在ValidateType注解，则需要导入ValidateType类
                    boolean existValidateType = false;
                    for (ValidateAnnotationBean bean : annotationBeanList) {
                        UseNeedImportClass useAnnotation = UseNeedImportClass.valueOf(bean.getName());
                        importContent.add(useAnnotation.getImportContent(project));
                        if (!existValidateType) {
                            existValidateType = UseNeedImportClass.Effective.equals(useAnnotation)
                                    || UseNeedImportClass.Unique.equals(useAnnotation);
                        }
                    }
                    if (existValidateType) {
                        importContent.add(UseNeedImportClass.ValidateType.getImportContent(project));
                    }
                } else {
                    String annContent = x.getAnnotationContent();
                    if (!DJJHelper.isBlank(annContent)) {
                        UseNeedImportClass useAnnotation = UseNeedImportClass.valueOf(annContent);
                        String annImport = useAnnotation.getImportContent(project);
                        importContent.add(annImport);
                    }
                }

                //如果是标记了QueryField注解，判断是否需要导入相应内容
                QueryFieldBean queryFieldBean = x.getQueryFieldBean();
                if (queryFieldBean != null) {
                    DJJConfigBean configBean = DJJState.getInstance(project).getConfigBean();
                    if (queryFieldBean.useSqlExpression()) {
                        importContent.add(configBean.getSqlExpression());
                    }
                    if (queryFieldBean.useQueryPresentCondition()) {
                        importContent.add(configBean.getQueryPresentCondition());
                    }
                }
                //字段类型本身是否需要导入
                if (PsiHelper.needImport(x.getPsiType())) {
                    importContent.add(x.getPsiType().getInternalCanonicalText());
                }
            });
        }

        default void handleAnnotationContent(PsiField psiField, HandleClassField handleClassField) {

        }
    }

    /**
     * 转换接口
     *
     * @param <T>
     */
    private interface ITransform<T extends IHandleClass> {
        T newHandleClass(String className, String classDescription);

        //模型类型 vo param
        String modelType();

        //自定义处理类
        void customHandleClass(PsiClass entityClass, T handleClass);

        //导入处理
        default void importHandle(Set<String> importContent, PsiJavaFile javaFile, PsiClass entityClass) {

        }

        void createUIFieldTable(T handleClass);


        //转换模版名
        String templateName();

        default Consumer<String> buildJavaClassQualifiedName() {
            return null;
        }

        //创建成功后是否展示消息
        default boolean showMessage() {
            return true;
        }
    }

}

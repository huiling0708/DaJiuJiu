package main.java.common;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import main.java.utils.FreemarkerUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * psi 文件帮助
 */
public abstract class PsiHelper {

    /**
     * 指定Psi类型是否需要导入
     *
     * @param psiType
     * @return
     */
    public static boolean needImport(PsiType psiType) {
        if (psiType == null) {
            return false;
        }
        if (TypeMapping.basicJavaType(psiType.getCanonicalText())) {
            return false;
        }
        if (psiType.getInternalCanonicalText().startsWith("java.lang")) {
            return false;
        }
        return true;
    }

    /**
     * 获取两个字段之间的换行元素
     *
     * @param psiClass
     * @return
     */
    public static PsiElement getFieldPsiWhiteSpace(PsiClass psiClass) {
        // 使用createFieldFromText方法创建字段时
        // 方法因格式限制，不允许以空行、空白字符或换行符开头
        // 所以生成的字段与上一个字段中间没有空行，看着很别捏
        // 尝试使用 createStatementFromText创建代码块，企图不校验格式，同样失败，格式错误
        // 因为在PSI文件解析中，空格和换行都使用的 PsiWhiteSpace元素，PsiWhiteSpace是PsiElement的子类
        // 尝试新建一个 PsiWhiteSpace
        // 在添加字段之前先添加 PsiWhiteSpace 再添加字段
        // 在元素工厂中未找到创建PsiWhiteSpace的方法，只找到了它的实现类，尝试直接实例化
        // PsiWhiteSpace space=new PsiWhiteSpaceImpl(""); 创建成功，以为完美解决
        // 结果在psiClass调用addAfter方法时发生 DummyHolderFactory.createHolder param is null 异常
        // 查看源码，发现addAfter方法中，在调用 createHolder 方法时，会从传入的PsiElement中获取PsiManager
        // 因为 PsiWhiteSpace 是通过实现类直接new 出来的，所以PsiManager为空
        // 未找到可以创建 PsiWhiteSpace 的其它方法
        // 因为没有找到完美的创建PsiWhiteSpace的方式，试图硬来，面向运气一波
        // 使用PsiTreeUtil工具，可以读取psi树，读取两个字段之间的元素
        // 果然出了字段元素本身之外，发现了 PsiWhiteSpace 元素
        // 使用元素自身的copy方法，复制一个新的 PsiWhiteSpace
        // 直接添加到最后一个字段后面 到此 换行成功
        PsiField[] fields = psiClass.getFields();
        if (fields.length < 2) {
            return null;
        }
        //取两个字段之间的换行元素 PsiWhiteSpace
        List<PsiElement> elementsOfRange =
                PsiTreeUtil.getElementsOfRange(fields[fields.length - 2],
                        fields[fields.length - 1]);
        return elementsOfRange.get(1).copy();
    }


    /**
     * 获取实体类描述
     *
     * @param entityClass
     * @return
     */
    public static String getEntityDescription(PsiClass entityClass) {
        //是否配置@Entity 实体类
        PsiAnnotation entityAnn = entityClass.getAnnotation("javax.persistence.Entity");
        if (entityAnn == null) {
            throw new RuntimeException("选择的类不是一个实体类");
        }
        return getApiModelDescription(entityClass);
    }

    /**
     * 获取类描述
     *
     * @param psiClass
     * @return
     */
    public static String getApiModelDescription(PsiClass psiClass) {
        //获取实体描述
        PsiAnnotation apiModelAnn = psiClass.getAnnotation(UseNeedImportClass.ApiModel.getImportContent());
        if (apiModelAnn == null) {
            throw new RuntimeException("选择的类未配置@ApiModel");
        }
        PsiAnnotationMemberValue memberValue = apiModelAnn.findAttributeValue("description");
        if (null == memberValue) {
            throw new RuntimeException("选择的实体类配置的@ApiModel注解中，未给description字段赋值");
        }
        //实体描述
        return memberValue.getText().replace("\"", "");
    }

    /**
     * 获取实体字段描述
     *
     * @param field
     * @return 返回空表示未读取到 ApiModelProperty 注解
     */
    public static String getEntityFieldDescription(PsiField field) {
        PsiAnnotation propertyAnn = field.getAnnotation(UseNeedImportClass.ApiModelProperty.getImportContent());
        if (propertyAnn == null) {
            return null;
        }
        PsiAnnotationMemberValue annValue = propertyAnn.findAttributeValue("value");
        if (annValue == null || annValue.getText() == null) {
            return field.getName();
        } else {
            return annValue.getText().replace("\"", "");
        }
    }

    /**
     * 根据鼠标所在元素 获取当前 PsiJava文件
     *
     * @param event
     * @return
     */
    public static PsiJavaFile getPsiJavaFile(AnActionEvent event) {

        PsiElement psiElement = event.getData(PlatformDataKeys.PSI_ELEMENT);//鼠标所在的元素
        if (psiElement == null) {
            return null;
        }
        PsiFile containingFile = psiElement.getContainingFile();
        if (containingFile == null || !(containingFile instanceof PsiJavaFile)) {
            throw new RuntimeException("选择的文件不是一个JAVA文件");
        }
        return (PsiJavaFile) containingFile;
    }

    /**
     * 根据模版创建java 文件
     *
     * @param project   项目
     * @param template  模版
     * @param data      模版数据
     * @param directory 目录
     * @param <T>
     */
    public static <T extends IJavaFileClass> void createJavaFile(Project project, String template, T data,
                                                                 PsiDirectory directory) {
        createJavaFile(project, template, data, directory, null);
    }

    /**
     * 根据模版创建java 文件
     *
     * @param project            项目
     * @param template           模版
     * @param data               模版数据
     * @param directory          目录
     * @param childDirectoryName 子目录
     * @param <T>
     */
    public static <T extends IJavaFileClass> void createJavaFile(Project project, String template, T data,
                                                                 PsiDirectory directory, String childDirectoryName) {
        WriteCommandAction.runWriteCommandAction(project, () ->
        {
            PsiDirectory childDirectory = null;
            if (childDirectoryName != null) {
                childDirectory = directory.findSubdirectory(childDirectoryName);
                if (childDirectory == null) {
                    childDirectory = directory.createSubdirectory(childDirectoryName);
                }
            }
            String javaCode = FreemarkerUtils.processJavaCode(project, template, data);
            PsiJavaFile newJavaFile = (PsiJavaFile) PsiFileFactory.getInstance(project)
                    .createFileFromText(
                            data.fileName(),
                            JavaFileType.INSTANCE,
                            javaCode);
            if (childDirectory != null) {
                childDirectory.add(newJavaFile);
            } else {
                directory.add(newJavaFile);
            }
        });
    }

    /**
     * 根据指定目录获取指定文件 如果已存在则重新输入文件名称
     *
     * @param directory
     * @param className
     * @return
     */
    public static String getAndCheckClassName(PsiDirectory directory, String className) {
        // 判断是否已经存在
        PsiFile file = directory.findFile(className + ".java");
        if (file == null) {
            return className;
        }
        className = DJJMessage.inputDialog("[" + className + "]已存在，请输入新的类名称",
                className);
        return getAndCheckClassName(directory, className);
    }

    /**
     * 检查名称是否重复
     *
     * @param directory
     * @param className
     * @return
     */
    public static boolean checkClassName(PsiDirectory directory, String className) {
        if (DJJHelper.isBlank(className)) {
            DJJMessage.errorDialog("类名不能为空");
            return false;
        }
        PsiFile file = directory.findFile(className + ".java");
        if (file == null) {
            return true;
        }
        DJJMessage.errorDialog("[" + className + "]已存在，请输入新的类名称");
        return false;
    }

    /**
     * 读取Doc注释，目前仅只读第一行非空白行
     *
     * @param docComment
     * @return
     */
    public static String getDocCommentText(PsiDocComment docComment) {
        if (docComment == null) {
            return "没写注释？？？";
        }
        for (PsiElement psiElement : docComment.getDescriptionElements()) {
            String text = psiElement.getText();
            if (text != null && !"".equals(text.trim())) {
                return text.trim();
            }
        }
        return "没写注释？？？";
    }
}

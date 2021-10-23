package main.java.form;

import com.intellij.ide.util.TreeJavaClassChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.SneakyThrows;
import main.java.common.DJJHelper;
import main.java.common.DJJMessage;
import main.java.config.DJJConfigBean;
import main.java.config.DJJState;
import main.java.config.DefaultConfigType;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * 插件配置窗体
 */
public class ConfigForm {
    private JPanel configPanel;
    private JTextField entitySuperclass;
    private JTextField singleParamClass;
    private JTextField controllerResultVo;
    private JTextField queryInterface;
    private JTextField queryProvide;
    private JButton entityButton;
    private JButton paramButton;
    private JButton resultButton;
    private JButton queryApiButton;
    private JButton queryAnnButton;
    private JLabel projectLabel;
    private JButton autoButton;
    private JTextField jpaWrapper;
    private JButton jpaWrapperButton;
    private JTextField copyPropertyUtils;
    private JButton copyPropertyUtilsButton;
    private JTextField dictAnnotation;
    private JButton dictAnnotationButton;
    private JTextField validateTypeEnum;
    private JButton validateTypeEnumButton;
    private JTextField baiduAppId;
    private JTextField baiduSecurityKey;


    private Project project;

    public ConfigForm(Project project) {
        this.project = project;

        this.projectLabel.setText(project.getName());
        this.entityButton.addMouseListener(new LoadListener(entitySuperclass));
        this.paramButton.addMouseListener(new LoadListener(singleParamClass));
        this.resultButton.addMouseListener(new LoadListener(controllerResultVo));
        this.queryApiButton.addMouseListener(new LoadListener(queryInterface));
        this.queryAnnButton.addMouseListener(new LoadListener(queryProvide, DefaultConfigType.QueryProvide));
        this.jpaWrapperButton.addMouseListener(new LoadListener(jpaWrapper));
        this.copyPropertyUtilsButton.addMouseListener(new LoadListener(copyPropertyUtils));
        this.dictAnnotationButton.addMouseListener(new LoadListener(dictAnnotation));
        this.validateTypeEnumButton.addMouseListener(new LoadListener(validateTypeEnum, DefaultConfigType.ValidateType));


        autoButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int findCount = 0;
                findCount += autoSetValue(DefaultConfigType.DateEntity, entitySuperclass);
                findCount += autoSetValue(DefaultConfigType.SingleParam, singleParamClass);
                findCount += autoSetValue(DefaultConfigType.ResultVo, controllerResultVo);
                findCount += autoSetValue(DefaultConfigType.IVo, queryInterface);
                findCount += autoSetValue(DefaultConfigType.QueryProvide, queryProvide);
                findCount += autoSetValue(DefaultConfigType.JpaWrapper, jpaWrapper);
                findCount += autoSetValue(DefaultConfigType.CommonUtils, copyPropertyUtils);
                findCount += autoSetValue(DefaultConfigType.DictDescribe, dictAnnotation);
                findCount += autoSetValue(DefaultConfigType.ValidateType, validateTypeEnum);

                if (findCount == 0) {
                    DJJMessage.errorDialog("未找到相关配置类，请手动指定");
                }
                if (!DJJHelper.isBlank(queryProvide.getText())) {
                    autoSetValueByTypeGroup(DefaultConfigType.QueryProvide);
                }
                if (!DJJHelper.isBlank(validateTypeEnum.getText())) {
                    autoSetValueByTypeGroup(DefaultConfigType.ValidateType);
                }
            }
        });
    }

    /**
     * 自动设置值
     *
     * @param type
     * @param jTextField
     * @return 是否找到对应的class 未找到返回0
     */
    private int autoSetValue(DefaultConfigType type, JTextField jTextField) {
        String autoValue = this.autoGetValue(type);
        if (DJJHelper.isBlank(autoValue)) {
            return 0;
        }
        //把获取到的包路径设置到指定的 jTextField 中
        jTextField.setText(autoValue);
        return 1;
    }

    private void autoSetValueByTypeGroup(DefaultConfigType type) {
        if (type == null) {
            return;
        } else if (DefaultConfigType.QueryProvide.equals(type)) {
            //如果选择了 QueryProvide注解，表示使用了QueryProvide相关引用类，则自动搜寻以下类容
            DJJConfigBean configBean = DJJState.getInstance(project).getConfigBean();
            configBean.setQueryField(autoGetValue(DefaultConfigType.QueryField));
            configBean.setJpaWrapper(autoGetValue(DefaultConfigType.JpaWrapper));
            configBean.setSqlExpression(autoGetValue(DefaultConfigType.SqlExpression));
            configBean.setQueryPresentCondition(autoGetValue(DefaultConfigType.QueryPresentCondition));
        } else if (DefaultConfigType.ValidateType.equals(type)) {
            DJJConfigBean configBean = DJJState.getInstance(project).getConfigBean();
            configBean.setEffectiveAnnotation(autoGetValue(DefaultConfigType.Effective));
            configBean.setUniqueAnnotation(autoGetValue(DefaultConfigType.Unique));
        }
    }

    /**
     * 自动获取值
     *
     * @param type
     * @return
     */
    public String autoGetValue(DefaultConfigType type) {
        //根据默认配置的类名 在项目下全局搜索，并把搜到的第一个完整类名返回
        GlobalSearchScope globalSearchScope = GlobalSearchScope.projectScope(project);
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project,
                type.name() + ".java", globalSearchScope);
        if (filesByName == null || filesByName.length < 1) {
            return null;
        }
        PsiJavaFile javaFile = (PsiJavaFile) filesByName[0].getOriginalFile();
        return javaFile.getClasses()[0].getQualifiedName();
    }

    /**
     * 加载配置类button 监听
     */
    private class LoadListener extends MouseAdapter {
        private JTextField textField;
        private DefaultConfigType groupType;

        private LoadListener(JTextField textField) {
            this.textField = textField;
            this.groupType = null;
        }

        private LoadListener(JTextField textField, DefaultConfigType groupType) {
            this.textField = textField;
            this.groupType = groupType;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            //打开类选择器
            TreeJavaClassChooserDialog selector = new TreeJavaClassChooserDialog(
                    "Choose", project);
            selector.show();
            PsiClass psiClass = selector.getSelected();
            if (psiClass == null) {
                return;
            }
            String qualifiedName = psiClass.getQualifiedName();
            this.textField.setText(qualifiedName);
            autoSetValueByTypeGroup(groupType);
        }
    }


    /**
     * 主面板
     *
     * @return
     */
    public JPanel getConfigPanel() {
        return configPanel;
    }

    /**
     * 展示面板包含的内容
     * 从配置bean 中读取
     */
    public void showText() {
        this.beanHandle((t, p, c) -> {
            String oldText = (String) p.getReadMethod().invoke(c);
            if (oldText == null) {
                oldText = "";
            }
            t.setText(oldText);
            return false;
        });
    }

    /**
     * 更新 bean 内容
     * 从 面板的JTextField中读取，并设置到对应的bean中
     */
    public void apply() {
        this.beanHandle((t, p, c) -> {
            String text = t.getText();
            p.getWriteMethod().invoke(c, text);
            return false;
        });
    }

    /**
     * 检查是否更新
     * 配置bean中的内容，是否与面板中JTextField中的内容一致
     *
     * @return
     */
    public boolean isModified() {
        return this.beanHandle((t, p, c) -> {
            String oldText = (String) p.getReadMethod().invoke(c);
            String text = t.getText();
            if (oldText == null) {
                return text != null && !text.equals("");
            }
            return !oldText.equals(text);
        });

    }

    /**
     * 配置bean 通用处理
     * 通过字段名称 把配置bean中String 类型的字段 与 容器中相同字段的JTextField 映射起来处理
     *
     * @param helper
     * @return
     */
    @SneakyThrows
    private boolean beanHandle(IHelper helper) {
        DJJConfigBean configBean = DJJState.getInstance(this.project).getConfigBean();
        BeanInfo beanInfo = Introspector.getBeanInfo(DJJConfigBean.class);
        PropertyDescriptor[] ps = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor p : ps) {
            Field field;
            try {
                field = ConfigForm.class.getDeclaredField(p.getName());
            } catch (NoSuchFieldException e) {
                continue;
            }
            field.setAccessible(true);
            if (!(JTextField.class.isAssignableFrom(field.getType()))) {
                continue;
            }
            JTextField textField = (JTextField) field.get(this);
            if (textField == null) {
                continue;
            }
            if (helper.todo(textField, p, configBean)) {
                return true;
            }
        }
        return false;
    }

    private interface IHelper {
        boolean todo(JTextField textField, PropertyDescriptor p, DJJConfigBean configBean) throws InvocationTargetException, IllegalAccessException;
    }
}

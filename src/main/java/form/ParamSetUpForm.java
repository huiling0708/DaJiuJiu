package main.java.form;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import main.java.bean.HandleClassField;
import main.java.bean.ValidateAnnotationBean;
import main.java.common.DJJHelper;
import main.java.common.DJJMessage;
import main.java.common.TypeMapping;
import main.java.common.UseNeedImportClass;
import main.java.component.BaseDJJForm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ParamSetUpForm extends BaseDJJForm {

    private final static String DICT = "dict";//字典关键字
    private final static String ADD_VALIDATE_TYPE_KEY_WORDS = "Add Type...";

    private JPanel mainPanel;

    private JButton OKButton;
    private JButton cancelButton;

    //字段
    private JTextField fieldText;
    private JComboBox fieldTypeBox;

    //验证类型
    @SupportType(group = "validateType",
            type = {"String", "Character", DICT,
                    "int", "long",
                    "Integer", "Long"})
    private JPanel validateTypePanel;
    private JRadioButton validateTypeEffectiveRB;
    private JRadioButton validateTypeUniqueRB;
    @GroupComponent(group = "validateType", content = "value = ValidateType.%s", mustInput = true)
    private JComboBox validateTypeBox;
    @GroupComponent(group = "validateType", content = "nullable = %s")
    private JCheckBox validateTypeNullableBox;
    @GroupComponent(group = "validateType", content = "inCompany = %s")
    private JCheckBox invalidateTypeInCompanyBox;
    @GroupComponent(group = "validateType", content = "message = \"%s\"")
    private JTextField validateTypeMessage;
    private JButton validateTypeResetBtn;
    @GroupComponent(group = "validateType")
    private ButtonGroup validateTypeGroup;

    //null 相关
    @SupportType(group = "null", type = {})
    private JPanel nullPanel;
    private JRadioButton nullNullRB;
    private JRadioButton nullNotNullRB;
    private JRadioButton nullNotBlankRB;
    private JRadioButton nullNotEmptyRB;
    private JButton nullResetBtn;
    @GroupComponent(group = "null")
    private ButtonGroup nullGroup;

    //size 相关
    @SupportType(group = "size",
            type = {"String", "Character", "Collection", "Map", "Array"})
    private JPanel sizePanel;
    private JRadioButton sizeRB;
    @GroupComponent(group = "size", content = "min = %s")
    private JTextField sizeMinText;
    @GroupComponent(group = "size", content = "max = %s")
    private JTextField sizeMaxText;
    private JButton sizeResetBtn;

    //decimal小 相关
    @SupportType(group = "decimalMin",
            type = {"String", "Character", "BigDecimal", "BigInteger",
                    "byte", "short", "int", "long",
                    "Byte", "Short", "Integer", "Long"})
    private JPanel decimalMinPanel;
    private JRadioButton decimalMinRB;
    @GroupComponent(group = "decimalMin", content = "value = \"%s\"", mustInput = true)
    private JTextField decimalMinValueText;
    @GroupComponent(group = "decimalMin", content = "inclusive = %s")
    private JCheckBox decimalMinInclusiveBox;
    private JButton decimalMinResetBtn;

    //decimal大相关
    @SupportType(group = "decimalMax",
            type = {"String", "Character", "BigDecimal", "BigInteger",
                    "byte", "short", "int", "long",
                    "Byte", "Short", "Integer", "Long"})
    private JPanel decimalMaxPanel;
    private JRadioButton decimalMaxRB;
    @GroupComponent(group = "decimalMax", content = "value = \"%s\"", mustInput = true)
    private JTextField decimalMaxValueText;
    @GroupComponent(group = "decimalMax", content = "inclusive = %s")
    private JCheckBox decimalMaxInclusiveBox;
    private JButton decimalMaxResetBtn;

    //min相关
    @SupportType(group = "min",
            type = {"BigDecimal", "BigInteger",
                    "byte", "short", "int", "long",
                    "Byte", "Short", "Integer", "Long"})
    private JPanel minPanel;
    private JRadioButton minRB;
    @GroupComponent(group = "min", content = "value = %s", mustInput = true)
    private JTextField minValueText;
    private JButton minResetBtn;

    //max相关
    @SupportType(group = "max",
            type = {"BigDecimal", "BigInteger",
                    "byte", "short", "int", "long",
                    "Byte", "Short", "Integer", "Long"})
    private JPanel maxPanel;
    private JRadioButton maxRB;
    @GroupComponent(group = "max", content = "value = %s", mustInput = true)
    private JTextField maxValueText;
    private JButton maxResetBtn;

    //数字相关
    @SupportType(group = "number",
            type = {"BigDecimal", "BigInteger",
                    "byte", "short", "int", "long", "float", "double",
                    "Byte", "Short", "Integer", "Long", "Float", "Double"})
    private JPanel numberPanel;
    private JRadioButton numberPositiveRB;
    private JRadioButton numberPositiveOrZeroRB;
    private JRadioButton numberNegativeRB;
    private JRadioButton numberNegativeOrZeroRB;
    private JButton numberResetBtn;
    @GroupComponent(group = "number")
    private ButtonGroup numberGroup;

    //时间相关
    @SupportType(group = "date",
            type = {"Date", "LocalDate", "LocalDateTime", "LocalTime"})
    private JPanel datePanel;
    private JRadioButton dateFutureRB;
    private JRadioButton dateFutureOrPresentRB;
    private JRadioButton datePastRB;
    private JRadioButton datePastOrPresentRB;
    private JButton dateResetBtn;
    @GroupComponent(group = "date")
    private ButtonGroup dateGroup;

    //表达式相关
    @SupportType(group = "pattern",
            type = "String")
    private JPanel patternPanel;
    private JRadioButton patternRB;
    @GroupComponent(group = "pattern", content = "value = \"%s\"", mustInput = true)
    private JTextField patternValueText;
    private JButton patternResetButton;


    private Map<String, DJJComponentBean> componentMap;// 容器通用处理map
    private JFrame frame;//当前窗口
    private boolean fieldTypeIsDict;//字段类型为字典

    public ParamSetUpForm(Project project, PsiClass entityClass, HandleClassField handleClassField, Consumer<List<ValidateAnnotationBean>> consumer) {
        if (handleClassField.getParamFrame() != null) {
            frame = handleClassField.getParamFrame();
            frame.setVisible(true);
            return;
        }
        JFrame jFrame = this.windowCenter("Param Set Up Annotation", this.mainPanel);
        this.frame = jFrame;
        jFrame.getRootPane().setDefaultButton(OKButton);
        //设置字段
        fieldText.setText(handleClassField.getName() + " " + handleClassField.getDescription());
        //加载数据类型box
        TypeMapping.loadTypeComboBox(fieldTypeBox);
        //判断类型是否是枚举类型
        PsiType psiType = handleClassField.getPsiType();
        if (DJJHelper.isDictType(project, psiType)) {
            fieldTypeBox.insertItemAt(handleClassField.getType(), 0);
            this.fieldTypeIsDict = true;
        }
        fieldTypeBox.setSelectedItem(handleClassField.getType());

        //容器公共处理
        this.componentHandle();
        //类型处理
        chooseTypeHandle(handleClassField.getType());
        //验证类型初始化处理
        this.validateTypeInitHandle(project, entityClass, handleClassField);
        //类型选择监听
        fieldTypeBox.addItemListener(e -> {
            String item = (String) e.getItem();
            if (ItemEvent.SELECTED == e.getStateChange()) {
                chooseTypeHandle(item);
            }
        });

        //ok
        OKButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    okHandle(handleClassField, consumer);
                    jFrame.setVisible(false);
                    //jFrame.dispose(); 创建参数类之后再释放资源
                } catch (Exception exception) {
                    DJJMessage.errorDialog(exception);
                }

            }
        });

        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                jFrame.setVisible(false);
                //jFrame.dispose(); 创建参数类之后再释放资源
            }
        });
    }

    /**
     * ok 处理
     */
    public void okHandle(HandleClassField handleClassField, Consumer<List<ValidateAnnotationBean>> consumer) {
        List<ValidateAnnotationBean> annotationBeanList = new ArrayList<>();
        componentMap.forEach((k, v) -> {
            JButton resetButton = v.getResetButton();
            //可以状态下，表示有值输入
            if (!resetButton.isEnabled()) {
                return;
            }
            //选择的注解
            List<JRadioButton> radios = v.getRadios();
            JRadioButton chooseRadio = null;
            for (JRadioButton radio : radios) {
                if (radio.isSelected()) {
                    chooseRadio = radio;
                    break;
                }
            }
            if (chooseRadio == null) {
                return;
            }
            String annName = chooseRadio.getText();
            String content = v.buildAnnotationContent(annName, this);
            annotationBeanList.add(new ValidateAnnotationBean(annName, content));
        });
        handleClassField.setParamFrame(frame);
        handleClassField.setType((String) fieldTypeBox.getSelectedItem());
        consumer.accept(annotationBeanList);
    }

    /**
     * 验证类型初始化设置
     *
     * @param project
     * @param psiClass
     */
    private void validateTypeInitHandle(Project project, PsiClass psiClass, HandleClassField handleClassField) {
        //读取ValidateType
        PsiClass validateTypeClass = JavaPsiFacade.getInstance(project).findClass(
                UseNeedImportClass.ValidateType.getImportContent(project),
                GlobalSearchScope.projectScope(project));
        validateTypeBox.addItem("");
        validateTypeBox.addItem(ADD_VALIDATE_TYPE_KEY_WORDS);
        for (PsiField field : validateTypeClass.getFields()) {
            if (field instanceof PsiEnumConstant) {
                PsiEnumConstant e = (PsiEnumConstant) field;
                if (field.getName().equals("ALL")) {
                    continue;
                }
                PsiExpressionList argumentList = e.getArgumentList();
                String text = argumentList.getExpressions()[0]
                        .getText().replace("\"", "");
                String item = field.getName() + " " + text;
                validateTypeBox.addItem(item);
            }
        }
        //box 监听 添加新的type值
        validateTypeBox.addItemListener(e -> {
            String item = (String) e.getItem();
            if (ItemEvent.SELECTED == e.getStateChange() && ADD_VALIDATE_TYPE_KEY_WORDS.equals(item)) {
                new ValidateTypeAddForm(project, psiClass, handleClassField.getName(), bean -> {
                    String newItem = bean.getTypeName() + " " + bean.getTypeDescribe();
                    validateTypeBox.addItem(newItem);
                    validateTypeBox.setSelectedItem(newItem);
                });
            }
        });
        //初始化已有注解内容
        String annContent = handleClassField.getAnnotationContent();
        if (DJJHelper.isBlank(annContent)) {
            return;
        }
        //因为初始化时只会默认赋值这3种注解，所以只处理这3种
        switch (UseNeedImportClass.valueOf(annContent)) {
            case NotNull:
                nullNotNullRB.setSelected(true);
                return;
            case NotBlank:
                nullNotBlankRB.setSelected(true);
                return;
            case Positive:
                numberPositiveRB.setSelected(true);
                return;
        }
    }

    /**
     * 类型处理
     *
     * @param type
     */
    private void chooseTypeHandle(String type) {
        if (type == null) {
            return;
        }
        //字符串
        if (type.equals(TypeMapping.VARCHAR.getJavaType())) {
            nullNotBlankRB.setEnabled(true);
            nullNotEmptyRB.setEnabled(true);
        } else {
            nullNotBlankRB.setEnabled(false);
            nullNotEmptyRB.setEnabled(false);
        }
        componentMap.forEach((k, v) -> {
            Set<String> supportTypes = v.getSupportTypes();
            if (supportTypes == null) {
                return;
            }
            String validateType = fieldTypeIsDict ? DICT : type;
            JPanel panel = v.getPanel();
            if (supportTypes.contains(validateType)) {
                panel.setVisible(true);
            } else {
                panel.setVisible(false);
            }
            setBeanNotEnabled(v);
        });
    }

    //容器通用处理
    @SneakyThrows
    private void componentHandle() {
        componentMap = new HashMap<>();
        //先找面板
        Field[] declaredFields = ParamSetUpForm.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (!JPanel.class.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            SupportType ann = field.getAnnotation(SupportType.class);
            if (ann == null) {
                continue;
            }
            DJJComponentBean bean = new DJJComponentBean();
            bean.setGroupName(ann.group());
            bean.setPanel((JPanel) field.get(this));
            if (ann.type().length > 0) {
                List<String> supportTypes = Arrays.asList(ann.type());
                bean.setSupportTypes(supportTypes.stream().collect(Collectors.toSet()));
            }
            componentMap.put(ann.group(), bean);
        }
        //加载完面板再加载 ButtonGroup 避免ButtonGroup 被先加载
        for (Field field : declaredFields) {
            GroupComponent ann = field.getAnnotation(GroupComponent.class);
            if (ann == null) {
                continue;
            }
            field.setAccessible(true);
            String group = ann.group();
            DJJComponentBean bean = componentMap.get(group);
            if (ButtonGroup.class.isAssignableFrom(field.getType())) {
                bean.setButtonGroup((ButtonGroup) field.get(this));
                continue;
            }
            bean.getHandleFields().add(field);
        }

        // 通用处理
        for (Map.Entry<String, DJJComponentBean> entry : componentMap.entrySet()) {
            DJJComponentBean bean = entry.getValue();
            //重置按钮是否可用
            this.resetButtonHandle(bean);
            this.radioChangeListener(bean);
        }
    }

    /**
     * 单选框 监听
     *
     * @param bean
     */
    private void radioChangeListener(DJJComponentBean bean) {
        List<JRadioButton> radios = bean.getRadios();
        radios.forEach(radio -> {
            radio.addChangeListener(e -> {
                boolean enabled = false;
                if (bean.getButtonGroup() != null) {
                    enabled = true;
                } else {
                    enabled = radio.isSelected();
                }
                if (enabled) {
                    //当前面板下其它容器均可使用
                    JPanel panel = bean.getPanel();
                    for (Component component : panel.getComponents()) {
                        if (component instanceof JRadioButton) {
                            continue;
                        }
                        component.setEnabled(true);
                    }
                } else {
                    setBeanNotEnabled(bean);
                }

            });
        });
    }

    //重置按钮点击监听
    private void resetButtonHandle(DJJComponentBean bean) {
        JButton resetButton = bean.getResetButton();
        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setBeanNotEnabled(bean);
            }
        });
    }

    //设置bean中容器为不启用
    private void setBeanNotEnabled(DJJComponentBean bean) {
        ButtonGroup buttonGroup = bean.getButtonGroup();
        //如果存在 组 则组删除 否则单个设置为false
        if (buttonGroup != null) {
            buttonGroup.clearSelection();
        } else {
            bean.getRadios().forEach(x -> x.setSelected(false));
        }
        bean.getTextFields().forEach(x -> x.setText(""));
        bean.getCheckBoxes().forEach(x -> x.setSelected(false));
        for (Component component : bean.getPanel().getComponents()) {
            if (component instanceof JRadioButton) {
                continue;
            }
            //其它容器均设置为不可以使用
            component.setEnabled(false);
        }
    }

    //支持类型
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface SupportType {
        String group();//分组名称

        String[] type();//支付的类型
    }

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface GroupComponent {
        String group();//分组名称

        String content() default "";//字段对应的注解内容

        boolean mustInput() default false;//必须有值
    }

    //容器分组
    @Getter
    @Setter
    private class DJJComponentBean {
        private String groupName;//分组名称
        private JPanel panel;//面板
        private List<JRadioButton> radios;//单选
        private ButtonGroup buttonGroup;//选择组
        private JButton resetButton;//重置按钮
        private List<JTextField> textFields;//文本
        private List<JCheckBox> checkBoxes;//选择
        private Set<String> supportTypes;//允许类型
        private List<Field> handleFields;//需要处理注解内容的字段

        public void setPanel(JPanel panel) {
            this.panel = panel;
            radios = new ArrayList<>();
            textFields = new ArrayList<>();
            checkBoxes = new ArrayList<>();
            handleFields = new ArrayList<>();
            Component[] components = panel.getComponents();
            for (Component component : components) {
                if (component instanceof JRadioButton) {
                    this.radios.add((JRadioButton) component);
                    continue;
                }
                //其它容器均设置为不可以使用
                if (component instanceof JButton) {
                    this.resetButton = (JButton) component;
                } else if (component instanceof JTextField) {
                    this.textFields.add((JTextField) component);
                } else if (component instanceof JCheckBox) {
                    checkBoxes.add((JCheckBox) component);
                }
                component.setEnabled(false);
            }
        }

        /**
         * 生成注解内容
         *
         * @param annName
         * @param form
         * @return
         */
        public String buildAnnotationContent(String annName, ParamSetUpForm form) {
            StringBuilder sub = new StringBuilder();
            sub.append("@");
            sub.append(annName);
            List<Field> handleFields = this.getHandleFields();
            if (handleFields.size() == 0) {
                return sub.toString();
            }
            sub.append("(");
            int i = 0;
            for (Field f : handleFields) {
                String content = getAnnotationContent(annName, f, form);
                //处理默认值
                if (DJJHelper.isBlank(content)) {
                    continue;
                }
                if (content.contains("inclusive = true")) {
                    continue;
                }
                if (content.contains("nullable = false")) {
                    continue;
                }
                if (content.contains("inCompany = false")) {
                    continue;
                }

                if (i > 0) {
                    sub.append(", ");
                }
                sub.append(content);
                i++;
            }
            sub.append(")");
            String result = sub.toString();
            if (i == 1) {
                if (result.contains("value =")) {
                    result = result.replace("value = ", "").trim();
                }
            }
            return result;
        }

        /**
         * 获取单个注解内容
         *
         * @param annName
         * @param field
         * @param form
         * @return
         */
        @SneakyThrows
        private String getAnnotationContent(String annName, Field field, ParamSetUpForm form) {
            GroupComponent ann = field.getAnnotation(GroupComponent.class);
            String content = ann.content();
            Class<?> type = field.getType();
            field.setAccessible(true);
            Object fieldValue = field.get(form);
            String value;
            if (JTextField.class.isAssignableFrom(type)) {
                JTextField jTextField = (JTextField) fieldValue;
                value = jTextField.getText();
            } else if (JCheckBox.class.isAssignableFrom(type)) {
                JCheckBox checkBox = (JCheckBox) fieldValue;
                value = checkBox.isSelected() ? "true" : "false";
            } else if (JComboBox.class.isAssignableFrom(type)) {
                JComboBox checkBox = (JComboBox) fieldValue;
                value = (String) checkBox.getSelectedItem();
                value = value.split(" ")[0];
            } else {
                value = "";
            }
            if (ADD_VALIDATE_TYPE_KEY_WORDS.equals(value)) {
                value = "";
            }
            if (DJJHelper.isBlank(value)) {
                if (ann.mustInput()) {
                    String message = ann.content();
                    if (message.contains("ValidateType")) {
                        message = "验证类型Type Value(枚举)";
                    } else {
                        message = ann.content().replace("=", "")
                                .replace("%s", "")
                                .replace("\"", "")
                                .trim();
                    }
                    throw new RuntimeException(String.format("选择标记@%s注解,必须指定[%s]值", annName, message));
                }
                return null;
            }
            return String.format(content, value).trim();
        }
    }
}

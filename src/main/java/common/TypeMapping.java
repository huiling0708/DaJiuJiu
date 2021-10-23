package main.java.common;

import lombok.Getter;
import main.java.bean.DataEntity;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 类型映射
 * MYSQL常用的数据库类型与java类型的转换
 */
@Getter
public enum TypeMapping {

    VARCHAR("String"),
    INT("int", "Integer"),
    INTEGER("int", "Integer"),
    BIGINT("long", "Long"),
    DOUBLE("double", "Double"),
    FLOAT("float", "Float"),
    BYTE("byte", "Byte"),
    DATE("Date", null, "java.util.Date"),
    DATETIME("Date", null, "java.util.Date"),
    DECIMAL("BigDecimal", null, "java.math.BigDecimal"),
    NUMERIC("BigDecimal", null, "java.math.BigDecimal"),
    TIME("Date", null, "java.util.Date"),
    TIMESTAMP("Date", null, "java.util.Date"),
    BIT("boolean", "Boolean"),
    CHAR("String"),
    CLOB("String"),
    TEXT("String"),
    ;


    TypeMapping(String javaType) {
        this.javaType = javaType;
        this.packageName = null;
        this.javaPackingType = null;
    }

    TypeMapping(String javaType, String javaPackingType) {
        this.javaType = javaType;
        this.javaPackingType = javaPackingType;
        this.packageName = null;
    }

    TypeMapping(String javaType, String javaPackingType, String packageName) {
        this.javaType = javaType;
        this.javaPackingType = javaType;
        this.packageName = packageName;
    }


    private String javaType;//java 类型
    private String javaPackingType;//java 包装类型
    private String packageName;//类型包路径

    /**
     * 获取 java 类型
     *
     * @param mandatory
     * @return
     */
    public String getJavaType(boolean mandatory) {
        return mandatory ? this.javaType : this.javaPackingType;
    }

    /**
     * 去掉类型长度
     *
     * @param dataType
     * @return
     */
    public static String clearDataTypeLength(String dataType) {
        if (dataType.indexOf("(") > 0) {
            return dataType.substring(0, dataType.indexOf("("));
        }
        return dataType;
    }

    /**
     * 根据数据库类型获取映射（类型需要去掉长度）
     *
     * @param dataType
     * @return
     */
    public static TypeMapping getTypeMappingByDataType(String dataType) {
        try {
            return TypeMapping.valueOf(dataType.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("未定义的数据库类型[" + dataType + "]");
        }
    }

    /**
     * 根据java 类型获取映射类型
     *
     * @param javaType
     * @return
     */
    public static TypeMapping getTypeMappingByJavaType(String javaType) {
        return JAVA_REVERSE_MAPPING.get(javaType);
    }

    private final static Set<String> BASIC_JAVA_TYPE;//java 基本类型
    private final static Map<String, String> JAVA_PACKING_TYPE_MAP;//封装类型映射
    private final static Map<String, TypeMapping> JAVA_REVERSE_MAPPING;//java 反向映射
    private final static JComboBox typeComboBox;//java 类型下拉选择
    private final static JComboBox entityTypeComboBox;//java 类型下拉选择
    public final static String DICT_KEYWORD = "Dict";//字典类型关键字
    public final static String ADD_DICT_KEYWORD = "Add Dict...";//添加字典类型关键字

    static {
        JAVA_PACKING_TYPE_MAP = new HashMap<>();
        //反向映射中，使用最靠前的数据类型
        JAVA_REVERSE_MAPPING = new HashMap<>();
        for (TypeMapping c : TypeMapping.values()) {
            TypeMapping typeMapping = JAVA_REVERSE_MAPPING.get(c.getJavaType());
            if (typeMapping == null) {
                JAVA_REVERSE_MAPPING.put(c.javaType, c);
            }
            String jpt = c.getJavaPackingType();
            //存在包装类型时
            if (jpt != null) {
                JAVA_PACKING_TYPE_MAP.put(c.javaType, c.javaPackingType);
                TypeMapping typeMappingByPacking = JAVA_REVERSE_MAPPING.get(jpt);
                if (typeMappingByPacking == null) {
                    JAVA_REVERSE_MAPPING.put(jpt, c);
                }
            }
        }
        JAVA_PACKING_TYPE_MAP.put("short", "Short");
        JAVA_PACKING_TYPE_MAP.put("char", "Character");


        BASIC_JAVA_TYPE = new LinkedHashSet<>();
        BASIC_JAVA_TYPE.add("int");
        BASIC_JAVA_TYPE.add("long");
        BASIC_JAVA_TYPE.add("double");
        BASIC_JAVA_TYPE.add("float");
        BASIC_JAVA_TYPE.add("byte");
        BASIC_JAVA_TYPE.add("short");
        BASIC_JAVA_TYPE.add("char");
        BASIC_JAVA_TYPE.add("boolean");

        typeComboBox = new JComboBox();
        loadTypeComboBox(typeComboBox);

        entityTypeComboBox = new JComboBox();
        entityTypeComboBox.addItem(DICT_KEYWORD);
        entityTypeComboBox.addItem(ADD_DICT_KEYWORD);
        loadTypeComboBox(entityTypeComboBox);
    }

    /**
     * 加载类型Box
     *
     * @param box
     */
    public static void loadTypeComboBox(JComboBox box) {
        box.addItem(VARCHAR.getJavaType());
        box.addItem(DATE.getJavaType());
        box.addItem(DECIMAL.getJavaType());
        BASIC_JAVA_TYPE.forEach(x -> {
            box.addItem(x);
            box.addItem(getPackingTypeByJavaType(x));
        });
    }

    /**
     * 是否为java 基本类型
     *
     * @param javaType
     * @return
     */
    public static boolean basicJavaType(String javaType) {
        return BASIC_JAVA_TYPE.contains(javaType);
    }

    /**
     * 获取主键生成策略
     *
     * @param dataEntity
     * @return
     */
    public static String primaryStrategy(DataEntity dataEntity) {
        switch (dataEntity.getPrimaryJavaType()) {
            case "String":
                dataEntity.getImportContent().add(UseNeedImportClass.GeneratedValue.getImportContent());
                dataEntity.getImportContent().add(UseNeedImportClass.GenericGenerator.getImportContent());
                return "@GenericGenerator(name = \"system-uuid\", strategy = \"uuid\")\n" +
                        "    @GeneratedValue(generator = \"system-uuid\")";
            case "int":
            case "Integer":
            case "long":
            case "Long":
                dataEntity.getImportContent().add(UseNeedImportClass.GeneratedValue.getImportContent());
                dataEntity.getImportContent().add(UseNeedImportClass.GenerationType.getImportContent());
                return "@GeneratedValue(strategy = GenerationType.IDENTITY)";
            default:
                return null;
        }
    }

    /**
     * 根据java 类型获取封装类型
     *
     * @param javaType
     * @return
     */
    public static String getPackingTypeByJavaType(String javaType) {
        String s = JAVA_PACKING_TYPE_MAP.get(javaType);
        return s == null ? javaType : s;
    }

    public static JComboBox getTypeComboBox() {
        return typeComboBox;
    }

    public static JComboBox getEntityTypeComboBox() {
        return entityTypeComboBox;
    }

    public static UseNeedImportClass getValidationAnnotationByType(String type) {
        switch (type) {
            case "String":
                return UseNeedImportClass.NotBlank;
            case "Date":
            case "BigDecimal":
            case "Integer":
            case "Long":
            case "Double":
                return UseNeedImportClass.NotNull;
            case "int":
            case "long":
            case "double":
                return UseNeedImportClass.Positive;
            default:
                if (!basicJavaType(type)) {
                    return UseNeedImportClass.NotNull;
                }
                return null;
        }
    }
}

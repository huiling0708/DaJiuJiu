package main.java.config;


import com.intellij.util.xmlb.annotations.Transient;
import lombok.Data;
import main.java.common.DJJHelper;
import main.java.common.DJJMessage;

import java.io.Serializable;

/**
 * 配置bean
 */
@Data
public class DJJConfigBean implements Serializable {

    private static final long serialVersionUID = -4216808068744066884L;

    private String entitySuperclass;//实体超类
    private String singleParamClass;//单字段查询参数类
    private String controllerResultVo;//控制器返回结果
    private String queryInterface;//查询接口
    private String queryProvide;//查询注解
    private String queryField;//查询字段注解

    private String jpaWrapper;//jpa封装
    private String copyPropertyUtils;//copyProperty工具类

    private String sqlExpression;//Sql表达式
    private String queryPresentCondition;//查询当前条件

    private String dictAnnotation;//数据字典注解
    private String dictPackage;//数据字典所在包

    private String validateTypeEnum;//验证类型枚举
    private String effectiveAnnotation;//验证有效注解
    private String uniqueAnnotation;//验证唯一注解

    private String baiduAppId;//百度翻译 AppId
    private String baiduSecurityKey;//百度翻译 SecurityKey
    @Transient
    private boolean check;//校验开关

    public String getEntitySuperclass() {
        this.checkValue(entitySuperclass, "EntitySuperclass");
        return entitySuperclass;
    }

    public String getQueryInterface() {
        this.checkValue(queryInterface, "QueryInterface");
        return queryInterface;
    }

    public String getCopyPropertyUtils() {
        this.checkValue(copyPropertyUtils, "copyPropertyUtils");
        return copyPropertyUtils;
    }

    public String getControllerResultVoAndCheck() {
        this.checkValue(true, controllerResultVo, "ControllerResultVo");
        return controllerResultVo;
    }

    public String getQueryProvideAndCheck() {
        this.checkValue(true, queryProvide, "queryProvide");
        return queryProvide;
    }

    public String getQueryFieldAndCheck() {
        this.checkValue(true, queryField, "queryField");
        return queryField;
    }

    public String getJpaWrapperAndCheck() {
        this.checkValue(true, jpaWrapper, "jpaWrapper");
        return jpaWrapper;
    }

    public String getCopyPropertyUtilsAndCheck() {
        this.checkValue(true, copyPropertyUtils, "copyPropertyUtils");
        return copyPropertyUtils;
    }

    public String getDictAnnotationAndCheck() {
        this.checkValue(true, dictAnnotation, "DictAnnotation");
        return dictAnnotation;
    }

    public String getDictPackageAndCheck() {
        this.checkValue(true, dictPackage, "DictPackage");
        return dictPackage;
    }

    public String getValidateTypeEnumAndCheck() {
        this.checkValue(true, validateTypeEnum, "ValidateTypeEnum");
        return validateTypeEnum;
    }

    public String getEffectiveAnnotationAndCheck() {
        this.checkValue(true, effectiveAnnotation, "ValidateTypeEnum");//配置ValidateTypeEnum自动读取
        return effectiveAnnotation;
    }

    public String getUniqueAnnotationAndCheck() {
        this.checkValue(true, uniqueAnnotation, "ValidateTypeEnum");//配置ValidateTypeEnum自动读取
        return uniqueAnnotation;
    }

    public void setDictAnnotation(String dictAnnotation) {
        if (DJJHelper.isBlank(dictAnnotation)) {
            return;
        }
        //字典注解存理应存放在字典包的base子包里
        int i = dictAnnotation.lastIndexOf(".dict.base");
        if (i < 0) {
            String message = "未在项目中找到数据字典Dict相关类，无法使用Dict相关功能";
            DJJMessage.errorDialog(message);
            throw new RuntimeException(message);
        }
        this.dictAnnotation = dictAnnotation;
        this.dictPackage = dictAnnotation.substring(0, i) + ".dict";
    }

    public String getEntitySuperclassSimpleName() {
        return DJJHelper.getSimpleName(this.getEntitySuperclass());
    }

    public String getQueryInterfaceSimpleName() {
        return DJJHelper.getSimpleName(this.getQueryInterface());
    }

    public String getCopyPropertyUtilsSimpleName() {
        return DJJHelper.getSimpleName(this.getCopyPropertyUtils());
    }

    public void checkValue(String value, String name) {
        this.checkValue(check, value, name);
    }

    public void checkValue(boolean check, String value, String name) {
        if (check && DJJHelper.isBlank(value)) {
            String message = String.format("未配置[%s],请先在Preferences DaJiuJiu选项卡中完成初始化配置", name);
            DJJMessage.errorDialog(message);
            throw new RuntimeException(message);
        }
    }
}

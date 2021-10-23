package main.java.common;

import com.intellij.openapi.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import main.java.config.DJJState;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 使用的注解
 */
@Getter
@AllArgsConstructor
public enum UseNeedImportClass {


    Null("javax.validation.constraints.Null"),
    NotNull("javax.validation.constraints.NotNull"),
    NotBlank("javax.validation.constraints.NotBlank"),
    NotEmpty("javax.validation.constraints.NotEmpty"),
    Size("javax.validation.constraints.Size"),
    DecimalMax("javax.validation.constraints.DecimalMax"),
    DecimalMin("javax.validation.constraints.DecimalMin"),
    Max("javax.validation.constraints.Max"),
    Min("javax.validation.constraints.Min"),
    Positive("javax.validation.constraints.Positive"),
    PositiveOrZero("javax.validation.constraints.PositiveOrZero"),
    Negative("javax.validation.constraints.Negative"),
    NegativeOrZero("javax.validation.constraints.NegativeOrZero"),
    Future("javax.validation.constraints.Future"),
    FutureOrPresent("javax.validation.constraints.FutureOrPresent"),
    Past("javax.validation.constraints.Past"),
    PastOrPresent("javax.validation.PastOrPresent"),
    Pattern("javax.validation.Pattern"),

    List("java.util.List"),
    ApiOperation("io.swagger.annotations.ApiOperation"),
    ApiModel("io.swagger.annotations.ApiModel"),
    ApiModelProperty("io.swagger.annotations.ApiModelProperty"),

    RestController("org.springframework.web.bind.annotation.RestController"),
    Controller("org.springframework.stereotype.Controller"),
    PostMapping("org.springframework.web.bind.annotation.PostMapping"),
    RequestBody("org.springframework.web.bind.annotation.RequestBody"),

    Autowired("org.springframework.beans.factory.annotation.Autowired"),
    Service("org.springframework.stereotype.Service"),
    Transactional("org.springframework.transaction.annotation.Transactional"),

    Entity("javax.persistence.Entity"),
    GeneratedValue("javax.persistence.GeneratedValue"),
    GenericGenerator("org.hibernate.annotations.GenericGenerator"),
    GenerationType("javax.persistence.GenerationType"),
    Id("javax.persistence.Id"),
    Column("javax.persistence.Column"),


    ControllerResultVo("") {
        @Override
        public String getImportContent(Project project) {
            return DJJState.getInstance(project).getConfigBean().getControllerResultVoAndCheck();
        }
    },
    QueryProvide("") {
        @Override
        public String getImportContent(Project project) {
            return DJJState.getInstance(project).getConfigBean().getQueryProvideAndCheck();
        }
    },
    QueryField("") {
        @Override
        public String getImportContent(Project project) {
            return DJJState.getInstance(project).getConfigBean().getQueryFieldAndCheck();
        }
    },
    JpaWrapper("") {
        @Override
        public String getImportContent(Project project) {
            return DJJState.getInstance(project).getConfigBean().getJpaWrapperAndCheck();
        }
    },
    CopyPropertyUtils("") {
        @Override
        public String getImportContent(Project project) {
            return DJJState.getInstance(project).getConfigBean().getCopyPropertyUtilsAndCheck();
        }
    },
    DictAnnotation("") {
        @Override
        public String getImportContent(Project project) {
            return DJJState.getInstance(project).getConfigBean().getDictAnnotationAndCheck();
        }
    },
    DictPackage("") {
        @Override
        public String getImportContent(Project project) {
            return DJJState.getInstance(project).getConfigBean().getDictPackageAndCheck();
        }
    },
    ValidateType("") {
        @Override
        public String getImportContent(Project project) {
            return DJJState.getInstance(project).getConfigBean().getValidateTypeEnumAndCheck();
        }
    },
    Effective("") {
        @Override
        public String getImportContent(Project project) {
            return DJJState.getInstance(project).getConfigBean().getEffectiveAnnotationAndCheck();
        }
    },
    Unique("") {
        @Override
        public String getImportContent(Project project) {
            return DJJState.getInstance(project).getConfigBean().getUniqueAnnotationAndCheck();
        }
    },

    ;

    private String importContent;
    private final static Set<String> NOT_NULL_ANN;

    static {
        NOT_NULL_ANN = new HashSet<>();
        NOT_NULL_ANN.add(NotBlank.name());
        NOT_NULL_ANN.add(NotNull.name());
        NOT_NULL_ANN.add(NotEmpty.name());
        NOT_NULL_ANN.add(Effective.name());
        NOT_NULL_ANN.add(Unique.name());
    }

    public String getImportContent(Project project) {
        return importContent;
    }

    /**
     * 存在 不能为空的注解
     *
     * @param value
     * @return
     */
    public static boolean existNotNullAnnotation(String value) {
        return NOT_NULL_ANN.contains(value);
    }
}

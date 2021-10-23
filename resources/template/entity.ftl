
import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
<#if config.entitySuperclass??>
import ${config.entitySuperclass};
</#if>
<#if data.primaryCount gt 1>
import javax.persistence.IdClass;
</#if>
<#if data.existDict>
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
</#if>
<#list data.importContent as ei>
import ${ei};
</#list>

@Data
@Entity
@Table(name = "${data.tableName!}")
@ApiModel(description = "${data.describe!}")
<#if data.primaryCount gt 1>
@IdClass(${data.className!}Pk.class)
</#if>
public class ${data.className!} extends ${config.entitySuperclassSimpleName!}<${data.className!}> {

    private static final long serialVersionUID = ${data.serialVersionUIDValue!};

<#list data.dataFields as field>
    <#if field.selected>
    <#if field.primary>
    @Id
        <#if data.primaryStrategy??>
    ${data.primaryStrategy!}
        </#if>
    </#if>
    @ApiModelProperty("${field.describe!}")
    @Column${field.jpaColumnContent!}
    <#if field.dictQualifiedName??>
    @Enumerated(EnumType.STRING)
    </#if>
    private ${field.javaType!} ${field.fieldName!};
    <#if field_has_next><#--换行-->

    </#if>
    </#if>
</#list>

}

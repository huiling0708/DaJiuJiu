
import ${config.queryInterface!};
import ${config.queryProvide!};
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
<#list data.importContent as ei>
import ${ei};
</#list>

@Data
@ApiModel(description = "${data.description!}")
@QueryProvide(value = "${data.queryName!}", entityType = ${data.entityClassName!}.class)
public class ${data.className!} implements ${config.queryInterfaceSimpleName!} {

    private static final long serialVersionUID = ${data.serialVersionUIDValue!};

    public ${data.className!}(${data.entityClassName!} ${data.entityClassNameParam!}) {
        this.copyProperty(${data.entityClassNameParam!});
    }

<#list data.fields as f>
    <#if f.selected>
        <#if f.existAnn>
    ${f.queryFieldContent!}
        </#if>
    @ApiModelProperty("${f.description!}")
    private ${f.type!} ${f.name!};
    <#if f_has_next>

    </#if>
    </#if>
</#list>

}


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
<#list data.importContent as ei>
import ${ei};
</#list>

@Data
@ApiModel(description = "${data.description!}")
public class ${data.className!} {

<#list data.fields as f>
    <#if f.selected>
    <#if f.existAnn>
        <#list f.annotationBeanList as bean>
    ${bean.content!}
        </#list>
    </#if>
    @ApiModelProperty(value = "${f.description!}"<#if f.notNull>, required = true</#if>)
    private ${f.type!} ${f.name!};
    <#if f_has_next>

    </#if>
    </#if>
</#list>

}

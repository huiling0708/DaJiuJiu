
import ${config.controllerResultVo!};
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
<#list data.importContent as ei>
import ${ei};
</#list>

@Api(tags = "${data.description!}-${data.classBeanName!}")
@RestController
@RequestMapping("${data.mappingValue!}")
public class ${data.controllerName!} {

    @Autowired
    private ${data.serviceName!} ${data.serviceParam!};

<#list data.methods as m>
    <#if m.selected>
    @ApiOperation(value = "${m.description!}")
    @PostMapping(value = "${m.mappingValue!}")
    public ResultVo<#if m.returnType??><${m.returnType}></#if> ${m.name!}(<#if m.paramType??>@Valid @RequestBody <#if m.singleParam>SingleParam<${m.paramType}><#else>${m.paramType}</#if> param</#if>) {
    <#if m.returnType??>
        return ResultVo.success(${data.serviceParam!}.${m.name!}(<#if m.paramType??>param<#if m.singleParam>.getKey()</#if></#if>));
    <#else>
        ${data.serviceParam!}.${m.name!}(<#if m.paramType??>param<#if m.singleParam>.getKey()</#if></#if>);
        return ResultVo.success();
    </#if>
    }
    <#if m_has_next>

    </#if>
    </#if>
</#list>
}
@ApiOperation(value = "${data.description!}")
@PostMapping(value = "${data.mappingValue!}")
public ResultVo<#if data.returnType??><${data.returnType}></#if> ${data.name!}(<#if data.paramType??>@Valid @RequestBody <#if data.singleParam>SingleParam<${data.paramType}><#else>${data.paramType}</#if> param</#if>) {
<#if data.returnType??>
    return ResultVo.success(${data.serviceParam!}.${data.serviceMethodName!}(<#if data.paramType??>param<#if data.singleParam>.getKey()</#if></#if>));
<#else>
    ${data.serviceParam!}.${data.serviceMethodName!}(<#if data.paramType??>param<#if data.singleParam>.getKey()</#if></#if>);
    return ResultVo.success();
</#if>
}
/**
* ${data.description!}
*
<#if data.inputParamSimple??>
* @param param
</#if>
<#if data.outputParamSimple?? && data.outputParamSimple != "void">
* @return
</#if>
*/
@Transactional
public ${data.outputParamSimple!} ${data.methodName!}(<#if data.inputParamSimple??>${data.inputParamSimple} param</#if>){

    <#if data.inputParamSimple??>
    ${data.relationEntitySimple!} ${data.entityParam!} = ${config.copyPropertyUtilsSimpleName!}.copyProperty(param, ${data.relationEntitySimple!}.class);
    <#else>
    ${data.relationEntitySimple!} ${data.entityParam!} = new ${data.relationEntitySimple!}();
    </#if>
    <#list data.saveNeedSetValueFields as f>
    //设置${f.description!}
    ${data.entityParam!}.set${f.nameParam!}();
    </#list>

    //保存${data.relationEntityDescription!}
    <#if data.outputParamSimple?? && data.outputParamSimple != "void">
    return ${data.entityParam!}.createWrapper().doSave();
    <#else>
    ${data.entityParam!}.createWrapper().doSave();
    </#if>
}
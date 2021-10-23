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
    <#if data.checkExists>
    //检查${data.relationEntityDescription!}是否存在
    JpaWrapper.create(${data.relationEntitySimple!}.class)
        <#list data.conditionFields as f>
          <#if f.selected>
            <#if f.existsParam>
            .where(${data.relationEntitySimple!}::get${f.nameParam!}, param.get${f.nameParam!}())
            <#else>
            .where(${data.relationEntitySimple!}::get${f.nameParam!}, ${f.name!})
            </#if>
          </#if>
        </#list>
            .doCheckNotExists("无效的${data.relationEntityDescription!}");
    </#if>

    //更新
    <#if data.outputParamSimple?? && data.outputParamSimple == "int">
    return JpaWrapper.create(${data.relationEntitySimple!}.class)
    <#else>
    JpaWrapper.create(${data.relationEntitySimple!}.class)
    </#if>
       <#list data.updateFields as f>
         <#if f.selected>
            <#if f.existsParam>
            .addUpdateValue(${data.relationEntitySimple!}::get${f.nameParam!}, param.get${f.nameParam!}())
            <#else>
            .addUpdateValue(${data.relationEntitySimple!}::get${f.nameParam!}, ${f.name!})
            </#if>
         </#if>
       </#list>
       <#list data.conditionFields as f>
         <#if f.selected>
            <#if f.existsParam>
            .where(${data.relationEntitySimple!}::get${f.nameParam!}, param.get${f.nameParam!}())
            <#else>
            .where(${data.relationEntitySimple!}::get${f.nameParam!}, ${f.name!})
            </#if>
         </#if>
       </#list>
            .doUpdate();
    <#if data.outputParamSimple?? && data.outputParamSimple != "int" && data.outputParamSimple != "void">

    //TODO 返回 ${data.outputParamSimple!}
    return null;
    </#if>
}
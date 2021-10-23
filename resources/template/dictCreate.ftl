
import ${config.dictPackage}.base.DictDescribe;
import ${config.dictPackage}.base.IDict;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@DictDescribe("${data.dictDescribe!}")
public enum ${data.dictName!} implements IDict<${data.dictName!}> {
<#list data.values as v>
    ${v.name!}("${v.describe!}"),
    <#if v_has_next>
    </#if>
</#list>
    ;

    private String describe;

}
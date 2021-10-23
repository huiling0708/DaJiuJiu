
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ${data.pkClassName!} implements Serializable {

    private static final long serialVersionUID = ${data.serialVersionUIDValue!};

<#list data.dataFields as field>
    <#if field.primary>
    private ${field.javaType!} ${field.fieldName!};<#if field.describe??>//${field.describe}</#if>
    </#if>
</#list>

}

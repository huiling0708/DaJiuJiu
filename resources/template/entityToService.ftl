
import org.springframework.stereotype.Service;
<#list data.importContent as ei>
import ${ei};
</#list>

/**
* ${data.description!}
*/
@Service
public class ${data.className!} {


}

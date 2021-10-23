package main.java.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.java.common.DJJHelper;
import main.java.common.ITemplateBean;

/**
 * 注入bean
 * 主要用于单独生成控制器的服务注入
 */
@Getter
@Setter
@NoArgsConstructor
public class AutowiredParamBean {
    private String name;//字段类型
    private String paramName;//参数名称

    public AutowiredParamBean(String name) {
        this.name = name;
        this.paramName = DJJHelper.firstLowerCase(name);
    }
}

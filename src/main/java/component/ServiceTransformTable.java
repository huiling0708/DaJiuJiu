package main.java.component;

import main.java.bean.HandleControllerMethod;
import main.java.common.TableDataHandle;

/**
 * Service服务转换为控制器时使用的表格
 */
public class ServiceTransformTable extends BaseDJJTable<HandleControllerMethod> {

    public ServiceTransformTable(TableDataHandle handle) {
        super(handle);
    }
}

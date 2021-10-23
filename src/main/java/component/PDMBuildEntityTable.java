package main.java.component;

import main.java.bean.DataEntity;
import main.java.bean.DictValueBean;
import main.java.common.TableDataHandle;

/**
 * pdm生成实体table
 */
public class PDMBuildEntityTable extends BaseDJJTable<DataEntity> {

    public PDMBuildEntityTable(TableDataHandle handle) {
        super(handle);
    }
}

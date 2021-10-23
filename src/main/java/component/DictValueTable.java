package main.java.component;

import com.intellij.openapi.project.Project;
import main.java.bean.DictValueBean;
import main.java.bean.HandleControllerMethod;
import main.java.common.DJJHelper;
import main.java.common.TableDataHandle;
import main.java.common.TypeMapping;
import main.java.utils.TransUtils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * 创建字典时的table
 */
public class DictValueTable extends BaseDJJTable<DictValueBean> {

    private Project project;

    public DictValueTable(TableDataHandle handle) {
        super(handle);
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    protected void init() {
        super.init();
        JTable table = this;
        this.getModel().addTableModelListener(e -> {
            if (project == null) {
                return;
            }
            int column = e.getColumn();//只对描述列处理
            if (column != 2) {
                return;
            }
//                if (table.isEditing()) {//编辑状态时不处理
//                    return;
//                }
            //英文是否已经存在值
            String enValue = (String) table.getValueAt(e.getFirstRow(), column - 1);
            if (!DJJHelper.isBlank(enValue)) {
                return;
            }
            String zhValue = (String) table.getValueAt(e.getFirstRow(), column);
            String result = TransUtils.zhToEn(project, zhValue);
            if (DJJHelper.isBlank(result)) {
                return;
            }
            table.setValueAt(result, e.getFirstRow(), column - 1);
        });
    }
}

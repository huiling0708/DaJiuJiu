package main.java.component;


import com.intellij.psi.PsiField;
import main.java.bean.HandleNonEditableField;
import main.java.bean.VoClassBean;
import main.java.common.DJJHelper;
import main.java.common.TableDataHandle;
import main.java.form.UIFieldTable;

import java.util.ArrayList;
import java.util.List;

/**
 * 向Vo类中添加字段时使用的表格
 */
public class VoClassAddFieldTable extends BaseDJJTable<VoClassBean> {

    public VoClassAddFieldTable(TableDataHandle<VoClassBean> handle) {
        super(handle);
    }

    @Override
    protected void fieldButtonHandle(TableButton button) {
        //点击最后一列button按钮，展示Vo类中的字段详情
        VoClassBean voClassBean = handle.getDataList().get(button.getRow());
        List<HandleNonEditableField> methodFields =
                HandleNonEditableField.buildFieldList(voClassBean.getPsiClass());

        //展示字段table form
        new UIFieldTable(new TableDataHandle<>(
                HandleNonEditableField.class, methodFields, false, false), h ->
                new NonEditableFieldTable(h));
    }
}

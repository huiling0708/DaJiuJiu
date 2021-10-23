package main.java.bean;

import lombok.Getter;
import lombok.Setter;
import main.java.common.IJavaFileClass;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典bean
 */
@Getter
@Setter
public class DictBean implements IJavaFileClass {

    private String dictName;//名称
    private String dictDescribe;//描述
    private String qualifiedName;//类限制名

    private List<DictValueBean> values = new ArrayList<>();

    @Override
    public String javaClassName() {
        return this.dictName;
    }
}

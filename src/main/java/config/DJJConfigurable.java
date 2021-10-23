package main.java.config;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import main.java.form.ConfigForm;

import javax.swing.*;

/**
 * 插件配置
 * 实现SearchableConfigurable 接口，可以在idea Preferences 配置中增加一个配置页面
 * 并在 plugin.xml 文件中，把该类指定为一个项目级别的配置
 * 则可以在构造函数中接收 Project 当前项目
 */
public class DJJConfigurable implements SearchableConfigurable {

    private ConfigForm configFrom;
    private Project project;

    public DJJConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public String getId() {
        return "DaJiuJiu-Configurable";
    }

    @Override
    public String getDisplayName() {
        return "DaJiuJiu";
    }

    @Override
    public JComponent createComponent() {
        if (configFrom == null) {
            configFrom = new ConfigForm(this.project);//创建配置窗体
        }
        configFrom.showText();//初始化窗体中展示的字段
        return configFrom.getConfigPanel();
    }

    @Override
    public boolean isModified() {
        return configFrom.isModified();//判断用户是否有修改
    }

    @Override
    public void apply() {
        configFrom.apply();//用户点击应用
    }

    @Override
    public void reset() {
        //用户点击重置，则重新初始化窗口类容
        configFrom.showText();
    }
}

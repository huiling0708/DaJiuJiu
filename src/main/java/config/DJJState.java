package main.java.config;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 配置持久化
 */
@State(name = "DJJState", storages = {@Storage(value = "DaJiuJiuConfig-settings.xml",
        roamingType = RoamingType.DISABLED)})
public class DJJState implements PersistentStateComponent<DJJConfigBean> {

    private DJJConfigBean configBean;

    public static DJJState getInstance(Project project) {
        return ServiceManager.getService(project, DJJState.class);
    }

    @Nullable
    @Override
    public DJJConfigBean getState() {
        return this.getConfigBean();
    }

    @Override
    public void loadState(@NotNull DJJConfigBean bean) {
        if (this.configBean == null) {
            this.configBean = this.getConfigBean();
        }
        //以xml的格式保存配置bean中的内容
        XmlSerializerUtil.copyBean(bean, this.configBean);
    }

    public DJJConfigBean getConfigBean() {
        if (configBean == null) {
            configBean = new DJJConfigBean();
        }
        return configBean;
    }
}

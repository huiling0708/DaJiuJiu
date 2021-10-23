package main.java.utils;

import com.intellij.openapi.project.Project;
import freemarker.template.Configuration;
import freemarker.template.Template;
import main.java.config.DJJConfigBean;
import main.java.config.DJJState;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 模版工具
 */
public abstract class FreemarkerUtils {

    /**
     * 生成java 代码
     *
     * @param project
     * @param templateName
     * @param data
     * @param <T>
     * @return
     */
    public static <T> String processJavaCode(Project project, String templateName, T data) {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setClassForTemplateLoading(FreemarkerUtils.class, "/template/");
        Template template = null;
        try {
            template = configuration.getTemplate(templateName);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("获取模板异常[" + templateName + "]");
        }
        DJJConfigBean configBean = DJJState.getInstance(project).getConfigBean();
        //打开校验
        configBean.setCheck(true);

        Map<String, Object> param = new HashMap<>();
        param.put("data", data);//源数据
        param.put("config", configBean);//配置数据
        StringWriter sw = new StringWriter();
        try {
            template.process(param, sw);
            String result = sw.toString();
            //mac
            result = result.replace("\r", "");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取模板解析异常[" + templateName + "]");
        } finally {
            configBean.setCheck(false);
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package main.java.utils;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import main.java.common.DJJHelper;
import main.java.config.DJJConfigBean;
import main.java.config.DJJState;
import main.java.utils.baidu.TransApi;
import main.java.utils.baidu.TransBean;
import main.java.utils.baidu.TransResultBean;

import java.util.List;

/**
 * 翻译工具
 */
public abstract class TransUtils {

    /**
     * 中文转英文
     *
     * @param value
     * @return
     */
    public static String zhToEn(Project project, String value) {
        return getResult(project, value, "zh", "en");
    }

    /**
     * 英文转中文
     *
     * @param value
     * @return
     */
    public static String enToZh(Project project, String value) {
        return getResult(project, value, "en", "zh");
    }

    /**
     * 转英文
     *
     * @param value
     * @return
     */
    public static String autoToEn(Project project, String value) {
        return getResult(project, value, "auto", "en");
    }

    /**
     * 转中文
     *
     * @param value
     * @return
     */
    public static String autoToZh(Project project, String value) {
        return getResult(project, value, "auto", "zh");
    }

    private static TransApi getAPi(Project project) {
        DJJConfigBean configBean = DJJState.getInstance(project).getConfigBean();
        String baiduAppId = configBean.getBaiduAppId();
        String baiduSecurityKey = configBean.getBaiduSecurityKey();
        if (DJJHelper.isBlank(baiduAppId) || DJJHelper.isBlank(baiduSecurityKey)) {
            return null;
        }
        return new TransApi(baiduAppId, baiduSecurityKey);
    }

    /**
     * 获取结果
     *
     * @param queryValue 待翻译值
     * @param from
     * @param to
     * @return
     */
    public static String getResult(Project project, String queryValue, String from, String to) {
        if (queryValue == null) {
            return null;
        }
        TransApi aPi = getAPi(project);
        if (aPi == null) {
            return null;
        }

        try {
            String transResult = aPi.getTransResult(queryValue, from, to);
            if (transResult == null) {
                return null;
            }
            TransBean transBean = JSONObject.parseObject(transResult, TransBean.class);
            if (transBean == null) {
                return null;
            }
            List<TransResultBean> trans_result = transBean.getTrans_result();
            if (trans_result == null || trans_result.size() == 0) {
                return null;
            }
            return trans_result.get(0).getDst();
        } catch (Exception e) {
            System.out.println("翻译失败：");
            e.printStackTrace();
            return null;
        }
    }
}

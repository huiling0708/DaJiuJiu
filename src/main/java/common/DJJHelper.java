package main.java.common;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiType;

import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 帮助
 */
public abstract class DJJHelper {

    /**
     * 去掉下划线 且 设置第一个字母为大小或小写
     *
     * @param value
     * @param upperFrist
     * @return
     */
    public static String clearLine(String value, boolean upperFrist) {
        value = value.toLowerCase();
        StringBuffer sub = new StringBuffer();
        for (char c : value.toCharArray()) {
            if (upperFrist) {
                sub.append(String.valueOf(c).toUpperCase());
                upperFrist = false;
                continue;
            }
            if (c == '_') {
                upperFrist = true;
                continue;
            }
            sub.append(c);
        }
        return sub.toString();
    }


    /**
     * 添加下划线并转大写
     * 0.如果全是大写则直接返回
     * 1.如果存在下划线则把小写字母转为大写并去掉空格
     * 2.如果仅存在空格 把空格替换为下划线 并转大写
     * 3.否则视为驼峰 把驼峰修改为 大写+下划线
     * 4.一些特殊的情况比如 小的又大写带下划线带空格等随缘
     *
     * @param value
     * @return
     */
    public static String addLineAndUpperCase(String value) {
        if (isBlank(value)) {
            return "";
        }
        value = value.trim();
        Pattern pattern = Pattern.compile("[A-Z]*");
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
            return value;
        }
        if (value.contains("_")) {
            //如果存在下划线则把小写字母转为大写并去掉空格
            StringBuffer sub = new StringBuffer();
            for (char c : value.toCharArray()) {
                if (Character.isLetter(c)) {
                    sub.append(Character.toUpperCase(c));
                } else {
                    sub.append(c);
                }
            }
            value = sub.toString().replaceAll(" ", "");
        } else if (value.contains(" ")) {
            //如果仅存在空格 把空格替换为下划线 并转大写
            value = value.replaceAll(" ", "_").toUpperCase();
        } else {
            //否则视为驼峰 把驼峰修改为 大写+下划线
            StringBuffer sub = null;
            for (char c : value.toCharArray()) {
                if (sub == null) {
                    sub = new StringBuffer();
                    sub.append(c);
                    continue;
                }
                if (Character.isUpperCase(c)) {
                    sub.append('_');
                }
                sub.append(c);
            }
            value = sub.toString().toUpperCase();
        }
        return value;
    }

    /**
     * 首字母大写
     *
     * @param value
     * @return
     */
    public static String firstToUpperCase(String value) {
        if (isBlank(value)) {
            return "";
        }
        return new StringBuilder()
                .append(Character.toUpperCase(value.charAt(0))).
                        append(value.substring(1))
                .toString();
    }

    /**
     * 首字母小写
     *
     * @param value
     * @return
     */
    public static String firstLowerCase(String value) {
        if (isBlank(value)) {
            return "";
        }
        return new StringBuilder()
                .append(Character.toLowerCase(value.charAt(0))).
                        append(value.substring(1))
                .toString();
    }

    /**
     * 生成控制器mapping 值
     *
     * @param value
     * @return
     */
    public static String buildMappingValue(String value) {
        if (isBlank(value)) {
            return "";
        }
        StringBuilder sub = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sub.append("/");
                c = Character.toLowerCase(c);
            }
            sub.append(c);
        }
        return sub.toString();
    }

    /**
     * 是否为空或空字符串
     *
     * @param cs
     * @return
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 生成一个 假 的序列号UID ，跟 1L 的效果一样，只是看起来像真的
     *
     * @return
     */
    public static String buildSerialVersionUID() {
        Random r = new Random();
        //第一位
        int firstValue = r.nextInt(8) + 1;
        StringBuilder sub = new StringBuilder();
        sub.append(firstValue);
        int i = 1;
        while (i < 19) {
            sub.append(r.nextInt(9));
            i++;
        }
        sub.append("L");
        return sub.toString();
    }

    /**
     * 根据完整名称获取简单名称
     *
     * @param name
     * @return
     */
    public static String getSimpleName(String name) {
        if (isBlank(name)) {
            return "";
        }
        int i = name.lastIndexOf(".");
        if (i < 0) {
            return name;
        }
        return name.substring(i + 1);
    }

    /**
     * 添加导入处理
     *
     * @param value
     * @param importContent
     */
    public static void addImport(String value, Set<String> importContent) {
        if (isBlank(value)) {
            return;
        }
        int i = value.indexOf("<");
        if (i < 0) {
            importContent.add(value);
            return;
        }
        importContent.add(value.substring(0, i));
        importContent.add(value.substring(i + 1, value.length() - 1));
    }

    /**
     * 判断是否是枚举类型
     *
     * @param project
     * @param psiType
     * @return
     */
    public static boolean isDictType(Project project, PsiType psiType) {
        if (psiType == null) {
            return false;
        }
        String internalCanonicalText = psiType.getInternalCanonicalText();
        String importContent = UseNeedImportClass.DictPackage.getImportContent(project);
        return internalCanonicalText.contains(importContent);
    }
}
package main.java.utils;

import main.java.bean.DataEntity;
import main.java.bean.DataField;
import main.java.common.DJJHelper;
import main.java.common.TypeMapping;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * SQL转换工具
 * 根据sql 语句生成 实体数据bean
 */
public class SQLConvertUtils {

    /**
     * 建表语句获取
     **/
    private final static String CREATE_TABLE_REGEX = "create\\s+table\\s+(.*?)\\s+\\(";//建表语句判断
    private final static String CREATE_TABLE_TABLE_NAME_REGEX = "(?<=table)\\s+(.*?)\\s+(?=\\()";//取表名
    private final static String CREATE_TABLE_COLUMN_REGEX = "(?<=\\()\\s*(.*?)\\s*primary key";//取列
    private final static String CREATE_TABLE_DADA_TYPE_LENGTH_REGEX = "(?<=\\()(.*?)(?=\\))";//取数据类型长度
    private final static String CREATE_TABLE_COLUMN_NOT_NUL_REGEX = "not\\s+nul(.*?)";//判断列是否允许为空
    private final static String CREATE_TABLE_PRIMARY_KEY_REGEX = "primary\\s+key(.*?)\\s+\\((.*?)\\)";//获取主键
    private final static String CREATE_TABLE_COMMENT_REGEX = "(?<=comment ')(.*?)(?=')";//获取备注
    /**
     * 查询语句获取
     **/
    private final static String SQL_QUERY_REGEX = "select(.*?)from";// sql 语句
    private final static String SQL_QUERY_COLUMN_REGEX = "(?<=select)(.*?)(?=from)";// 取列
    private final static String SQL_QUERY_COLUMN_ALIAS_REGEX = "(?<=')(.*?)(?=')";// 取别名
    private final static String SQL_QUERY_GET_FRIST_BLANK_REGEX = "(.*?)\\s+";// 截取到第一个空格


    /**
     * 根据sql 语句获取数据实体
     *
     * @param sql
     * @return
     */
    public static DataEntity buildDataBySQL(String sql) {
        if (StringUtils.isBlank(sql)) {
            throw new RuntimeException("SQL语句为空");
        }
        //先判断是否是建表语句
        Matcher matcher = getMatcher(sql, CREATE_TABLE_REGEX);
        if (matcher.find()) {
            return buildByCreateTableSQL(sql);
        }
        return buildBySelectSQL(sql);
    }

    /**
     * 根据 select 语句生成数据实体
     *
     * @param sql
     * @return
     */
    private static DataEntity buildBySelectSQL(String sql) {
        DataEntity dataEntity = new DataEntity();
        String s = sql.replaceAll("\r|\n|`", " ");
        Matcher matcher = getMatcher(s, SQL_QUERY_REGEX);
        //获取表名
        if (matcher.find()) {
            int end = matcher.end();
            String substring = s.substring(end);
            String tableName = getMatcherOne(substring.trim() + " x", SQL_QUERY_GET_FRIST_BLANK_REGEX).trim();
            dataEntity.setTableName(tableName);
            dataEntity.setClassName(DJJHelper.clearLine(tableName, true));
        } else {
            throw new RuntimeException("不支持的SQL语句类型,当前仅支持:\n[CREATE TABLE建表语句]\n[SELECT查询语句]");
        }
        //处理列
        String matcherOne = getMatcherOne(s, SQL_QUERY_COLUMN_REGEX);
        for (String value : matcherOne.split(",")) {
            value = value.trim();
            Matcher columnMatcher = getMatcher(value, SQL_QUERY_GET_FRIST_BLANK_REGEX);
            String column;
            //是否取了别名
            String alias = null;//别名
            if (columnMatcher.find()) {
                // ?.column_name as 'columnName'
                column = columnMatcher.group();
                //取出别名
                alias = getMatcherOne(value, SQL_QUERY_COLUMN_ALIAS_REGEX);
            } else {
                //?.column_name
                column = value;
            }
            //处理函数 ?(?.column_name)
            Matcher functionMatcher = getMatcher(column, CREATE_TABLE_DADA_TYPE_LENGTH_REGEX);
            if (functionMatcher.find()) {
                column = functionMatcher.group();
            }
            //去除点 ?.column_name
            int indexOf = column.indexOf(".");
            if (indexOf > 0) {
                column = column.substring(indexOf + 1).trim();
            }
            // 设置列名与java字段名称，如果存在别名则使用别名作为java 字段名称
            column = column.toLowerCase();
            DataField dataField = new DataField();
            dataField.setColumn(column);
            if (alias == null) {
                dataField.setFieldName(DJJHelper.clearLine(column, false));
            } else {
                dataField.setFieldName(alias);
            }
            dataEntity.addField(dataField);
        }
        return dataEntity;
    }

    /**
     * 根据建表语句生成数据实体
     *
     * @param sql
     * @return
     */
    private static DataEntity buildByCreateTableSQL(String sql) {
        DataEntity dataEntity = new DataEntity();
        //先处理换行与安全符
        sql = sql.replaceAll("\r|\n|`", "");
        //获取表名
        String tableName = getMatcherOne(sql, CREATE_TABLE_TABLE_NAME_REGEX).trim();
        dataEntity.setTableName(tableName);
        dataEntity.setClassName(DJJHelper.clearLine(tableName, true));

        //处理列 key type ... default null | not null ,key2 type2 ... default null | not null ...primary key
        String columns = getMatcherOne(sql, CREATE_TABLE_COLUMN_REGEX);
        //先按,分割
        String[] columnsSp = columns.split(",(?!\\d+)");
        //主键设置
        Map<String, DataField> keys = new HashMap<>();
        //跳过最后一列主键 primary key
        for (int i = 0; i < columnsSp.length - 1; i++) {
            DataField dataField = new DataField();
            String columnsValue = columnsSp[i];
            //取备注
            Matcher commentMatcher = getMatcher(columnsValue + "'", CREATE_TABLE_COMMENT_REGEX);
            if (commentMatcher.find()) {
                dataField.setDescribe(commentMatcher.group());
            }
            //按空格分割 其中 0=key,1 类型
            String[] split = columnsValue.trim().split("\\s+");
            //字段设置

            String columnName = split[0].trim();//列名
            dataField.setColumn(columnName);
            dataField.setFieldName(DJJHelper.clearLine(columnName, false));
            //是否允许为空 含有 default null 为允许，否则 不允许
            boolean notNull = getMatcher(columnsValue, CREATE_TABLE_COLUMN_NOT_NUL_REGEX).find();
            dataField.setMandatory(notNull);

            setDataType(dataEntity, dataField, split[1]);//字段类型
            dataEntity.addField(dataField);
            keys.put(columnName, dataField);
        }
        //处理主键
        String matcherOne = getMatcherOne(getMatcherOne(sql, CREATE_TABLE_PRIMARY_KEY_REGEX), CREATE_TABLE_DADA_TYPE_LENGTH_REGEX);
        String[] split = matcherOne.split(",");
        for (String s : split) {
            DataField dataField = keys.get(s.trim());
            if (dataField != null) {
                dataField.setPrimary(true);
                dataField.setMandatory(true);
                dataField.setJavaType(TypeMapping.getTypeMappingByDataType(dataField.getDataType()).getJavaType(true));
                dataEntity.addPrimaryCount(dataField.getJavaType());
            }
        }
        dataEntity.initPrimaryStrategy();
        return dataEntity;
    }

    /**
     * 设置字段类型
     *
     * @param dataField
     * @param dataType
     */
    private static void setDataType(DataEntity dataEntity, DataField dataField, String dataType) {
        Matcher matcher = getMatcher(dataType, CREATE_TABLE_DADA_TYPE_LENGTH_REGEX);
        //包含 括号,有长度
        if (matcher.find()) {
            String group = matcher.group();
            //是否有精度
            if (group.contains(",")) {
                String[] split = group.split(",");
                dataField.setPrecision(Integer.valueOf(split[0]));
                dataField.setScale(Integer.valueOf(split[1]));
            } else {
                dataField.setLength(Integer.valueOf(group));
            }
            dataType = TypeMapping.clearDataTypeLength(dataType);
        }
        dataField.setDataType(dataType);//数据类型
    }

    /**
     * 获取单个匹配器
     *
     * @param value
     * @param regex
     * @return
     */
    private static String getMatcherOne(String value, String regex) {
        Matcher matcher = getMatcher(value, regex);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new RuntimeException("SQL语句有误，请核对后重试");
    }

    /**
     * 获取匹配器
     *
     * @param value
     * @param regex
     * @return
     */
    private static Matcher getMatcher(String value, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(value);
    }

}

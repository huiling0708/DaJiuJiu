package main.java.bean;

import lombok.Getter;
import lombok.Setter;
import main.java.common.DJJHelper;
import main.java.common.UseNeedImportClass;

/**
 * 查询字段bean
 * 为QueryField注解服务
 * 主要用于显示 QueryProvideForm窗体中的组件值
 */
@Getter
@Setter
public class QueryFieldBean {
    private boolean mustInput = false;//是否必输
    private String condition = "EQUALS";//条件
    private boolean queryNullable = false;//允许查询空值
    private String present = "NONE";//查询当前条件
    private String fixedValue = "";//固定值

    public boolean useSqlExpression() {
        if (DJJHelper.isBlank(condition)) {
            return false;
        }
        return !"EQUALS".equals(this.condition.split(" ")[0]);
    }

    public boolean useQueryPresentCondition() {
        if (DJJHelper.isBlank(present)) {
            return false;
        }
        return !"NONE".equals(this.present.split(" ")[0]);
    }

    public String buildQueryFieldContent() {
        boolean needComma = false;

        StringBuilder sub = new StringBuilder();
        sub.append("@");
        sub.append(UseNeedImportClass.QueryField.name());
        //是否必输
        if (mustInput) {
            sub.append("(");
            sub.append("mustInput = true");
            needComma = true;
        }
        //条件
        String conditionValue = this.condition.split(" ")[0];
        if (!"EQUALS".equals(conditionValue)) {
            this.addComma(needComma, sub);
            sub.append("condition = SqlExpression.");
            sub.append(conditionValue);
            needComma = true;
        }
        //允许查询空值
        if (queryNullable) {
            this.addComma(needComma, sub);
            sub.append("queryNullable = true");
            needComma = true;
        }
        //当前条件
        String presentValue = this.present.split(" ")[0];
        if (!"NONE".equals(presentValue)) {
            this.addComma(needComma, sub);
            sub.append("present = QueryPresentCondition.");
            sub.append(presentValue);
            needComma = true;
        }
        //固定值
        if (!DJJHelper.isBlank(this.fixedValue)) {
            this.addComma(needComma, sub);
            sub.append("fixedValue = \"");
            sub.append(this.fixedValue);
            sub.append("\"");
            needComma = true;
        }
        if (needComma) {
            sub.append(")");
        }
        return sub.toString();
    }

    private void addComma(boolean needComma, StringBuilder sub) {
        if (needComma) {
            sub.append(", ");
        } else {
            sub.append("(");
        }
    }
}

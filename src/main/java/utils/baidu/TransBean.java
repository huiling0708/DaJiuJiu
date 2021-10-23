package main.java.utils.baidu;


import lombok.Data;

import java.util.List;

/**
 * 翻译bean
 */
@Data
public class TransBean {
    private String from;
    private String to;
    private List<TransResultBean> trans_result;
}

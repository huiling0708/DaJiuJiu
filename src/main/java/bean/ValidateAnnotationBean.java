package main.java.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ValidateAnnotationBean {

    private String name;//注解名称
    private String content;//内容
}

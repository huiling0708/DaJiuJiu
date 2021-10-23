package main.java.common;

/**
 * java 文件类
 * 表示该类可以创建一个java 文件
 */
public interface IJavaFileClass {

    default String fileName() {
        return javaClassName() + ".java";
    }

    String javaClassName();
}

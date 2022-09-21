package com.xxywebsite.bytecode.test;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class JavaAgentTest {
    public static int add(int num1, int num2) {
        return num1 + num2;
    }
    // java -javaagent /Users/xuxiaoyin/Projects/bytecode-exploration/sample/target/bytecode-manipulation-sample-1.0-SNAPSHOT.jar
    public static void main(String[] args) {
        add(3, 4);
    }
}

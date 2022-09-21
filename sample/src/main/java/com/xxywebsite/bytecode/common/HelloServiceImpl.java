package com.xxywebsite.bytecode.common;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class HelloServiceImpl implements HelloService {
    @Override
    public void sayHello(String name) {
        System.out.println(String.format("Hello %s", name));
    }
}

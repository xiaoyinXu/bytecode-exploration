package com.xxywebsite.bytecode.random;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author xuxiaoyin
 * @since 2022/9/26
 **/
public class FixedValueTest {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = new ByteBuddy()
                .subclass(Object.class)
                .name("MyObject")
                .method(ElementMatchers.named("toString"))
                .intercept(FixedValue.value("123"))
                .make()
                .load(FixedValueTest.class.getClassLoader())
                .getLoaded();

        System.out.println(clazz.newInstance().toString());
    }
}

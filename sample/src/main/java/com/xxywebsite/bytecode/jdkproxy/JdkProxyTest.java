package com.xxywebsite.bytecode.jdkproxy;

import com.xxywebsite.bytecode.common.HelloService;
import com.xxywebsite.bytecode.common.HelloServiceImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class JdkProxyTest {
    public static void main(String[] args) {
        HelloService helloService = (HelloService) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class<?>[]{HelloService.class},
                new LogInvocationHandler(new HelloServiceImpl()));
        helloService.sayHello("JdkProxy");
    }

    private static class LogInvocationHandler implements InvocationHandler {
        private Object instance;

        public LogInvocationHandler(Object instance) {
            this.instance = instance;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            long startTs = System.currentTimeMillis();
            try {
                return method.invoke(instance, args);
            } finally {
                System.out.println(String.format("方法:%s, 共耗时%dms", method.getName(), System.currentTimeMillis() - startTs));
            }
        }
    }
}

package com.xxywebsite.bytecode.bytebuddy;

import com.xxywebsite.bytecode.common.HelloService;
import com.xxywebsite.bytecode.common.HelloServiceImpl;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class ByteBuddyProxyTest {
    public static class LogImplementation {
        @RuntimeType
        public static Object invoke(@Origin Method method, @SuperCall Callable<?> callable) {
            long startTs = System.currentTimeMillis();
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println(String.format("方法:%s, 耗时:%dms", method.getName(), System.currentTimeMillis() - startTs));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Class<? extends HelloServiceImpl> clazz = new ByteBuddy()
                .subclass(HelloServiceImpl.class)
                .method(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                .intercept(MethodDelegation.to(LogImplementation.class))

                .make()
                .load(ClassLoader.getSystemClassLoader())
                .getLoaded();

        HelloService helloService = clazz.newInstance();
        helloService.sayHello("Cookie");
    }
}

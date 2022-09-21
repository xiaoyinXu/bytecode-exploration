package com.xxywebsite.bytecode.bytebuddy;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class MyByteBuddyLogAgent {
    public static class LogImplementation {
        @RuntimeType
        public static Object invoke(@Origin Method method, @SuperCall Callable<?> callable) {
            long startTs = System.currentTimeMillis();
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println(String.format("执行:%s方法，耗时:%dms", method.getName(), System.currentTimeMillis() - startTs));
            }
        }
    }

    public static void premain(String args, Instrumentation instrumentation) {
        new AgentBuilder.Default()
                .type(ElementMatchers.nameStartsWith("com.xxywebsite"))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(ElementMatchers.any())
                                .intercept(MethodDelegation.to(LogImplementation.class))
                ).installOn(instrumentation);
    }
}

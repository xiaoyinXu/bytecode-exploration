package com.xxywebsite.bytecode.bytebuddy;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;

/**
 * @author xuxiaoyin
 * @since 2022/9/27
 **/
public class AdviceTest {
    public static class A {
        public static int add(int num1, int num2) {
            return num1 + num2;
        }
    }

    public static class LogAdvice {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.Local("startTs") long startTs) {
            startTs = System.currentTimeMillis();
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Local("startTs") long startTs, @Advice.AllArguments Object[] allArguments, @Advice.Origin("#m") String methodName) {
            long endTs = System.currentTimeMillis();
            System.out.println(String.format("方法:%s, 入参为:%s, 共耗时:%dms", methodName, Arrays.toString(allArguments), endTs - startTs));
        }
    }

    public static void main(String[] args) {
        A.add(3, 4);
        Instrumentation instrumentation = ByteBuddyAgent.install();
        new AgentBuilder
                .Default()
                .disableClassFormatChanges()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .type(ElementMatchers.named("com.xxywebsite.bytecode.bytebuddy.AdviceTest$A"))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                        return builder.visit(Advice.to(LogAdvice.class).on(ElementMatchers.named("add")));
                    }
                })
                .installOn(instrumentation);

        A.add(3, 4);
    }

}

package com.xxywebsite.bytecode.bytebuddy;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * @author xuxiaoyin
 * @since 2022/9/26
 **/
public class ModifyObjectToStringTest {
    // 修改Object toString方法，并重新加载Object类
    public static void main(String[] args) {
        Object o = new Object();
        System.out.println(o.toString());

        // self attaching
        Instrumentation instrumentation = ByteBuddyAgent.install();

        new AgentBuilder
                .Default()
                .ignore(nameStartsWith("zxczczx.zxczc.xzczxc")) // 这一行的意义？ TODO
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(named("java.lang.Object"))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                        return builder
                                .method(named("toString"))
                                .intercept(FixedValue.value("123"));
                    }
                })
                .installOn(instrumentation);


        System.out.println(o.toString()); // 123
    }
}

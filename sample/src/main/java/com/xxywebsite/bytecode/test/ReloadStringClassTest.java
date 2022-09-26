package com.xxywebsite.bytecode.test;

import net.bytebuddy.agent.ByteBuddyAgent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @author xuxiaoyin
 * @since 2022/9/26
 **/
public class ReloadStringClassTest {
    public static void main(String[] args) throws Exception {
        // self-attach
        Instrumentation instrumentation = ByteBuddyAgent.install();

        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if (className.replace("/", ".").equals("java.lang.String")) {
                    System.out.println("java.lang.String被重新加载");
                }
                return classfileBuffer;
            }
        }, true);
        instrumentation.retransformClasses(String.class); // 打印java.lang.String被重新加载
        instrumentation.retransformClasses(String.class); // 打印java.lang.String被重新加载（可被重新加载多次）
    }
}

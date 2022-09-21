package com.xxywebsite.bytecode.common;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class JavaAgentTemplate {
    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(
                new ClassFileTransformer() {
                    @Override
                    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                        // logic
                        System.out.println(String.format("%s被加载", className));

                        return classfileBuffer;
                    }
                }
        );
    }
}

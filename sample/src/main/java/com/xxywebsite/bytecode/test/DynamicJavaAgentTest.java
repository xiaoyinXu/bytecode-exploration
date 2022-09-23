package com.xxywebsite.bytecode.test;

import net.bytebuddy.agent.ByteBuddyAgent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xuxiaoyin
 * @since 2022/9/23
 **/
public class DynamicJavaAgentTest {
    public static void main(String[] args) {
        // 1、attach当前JVM
        // 2、找到byte-buddy-agent.jar的文件地址
        // 3、调用VirtualMachine的loadAgent方法
        // 4、返回instrumentation实例
        Instrumentation instrumentation = ByteBuddyAgent.install();


        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                // your logic
                // 一旦获取到instrumentation实例后，你可以任意使用javassist/asm/cglib/byte-buddy


                System.out.println(String.format("%s被加载", className));
                return classfileBuffer;
            }
        });


        new ArrayBlockingQueue<Integer>(10);  // 打印 java/util/concurrent/ArrayBlockingQueue被加载
    }
}

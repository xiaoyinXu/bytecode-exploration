package com.xxywebsite.bytecode.asm;

import com.xxywebsite.bytecode.common.ByteArrayClassLoader;
import org.objectweb.asm.*;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class RemoveClassAnnotationTest {
    @Deprecated
    public static class A {

    }

    public static void main(String[] args) throws Exception {
        ClassReader classReader = new ClassReader("com.xxywebsite.bytecode.asm.RemoveClassAnnotationTest$A");
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9, classWriter) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if ("Ljava/lang/Deprecated;".equals(descriptor)) {
                    return null; // 去掉@Deprecared注解
                }
                return super.visitAnnotation(descriptor, visible);
            }
        };
        classReader.accept(classVisitor, 0);
        byte[] classBytes = classWriter.toByteArray(); // 已经去掉了@Deprecated后的字节码


        // 测试
        ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader();
        Class<?> clazz = byteArrayClassLoader.defineClazz(classBytes);
        System.out.println(clazz.isAnnotationPresent(Deprecated.class)); // false
    }
}

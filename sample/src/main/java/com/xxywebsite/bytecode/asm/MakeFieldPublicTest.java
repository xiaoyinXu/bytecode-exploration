package com.xxywebsite.bytecode.asm;

import com.xxywebsite.bytecode.common.ByteArrayClassLoader;
import org.objectweb.asm.*;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class MakeFieldPublicTest {
    public static class Student {
        private String name;
    }

    public static void main(String[] args) throws Exception {
        ClassReader classReader = new ClassReader("com.xxywebsite.bytecode.asm.MakeFieldPublicTest$Student");
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9, classWriter) {
            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                return super.visitField(Opcodes.ACC_PUBLIC, name, descriptor, signature, value);
            }
        };
        classReader.accept(classVisitor, 0);
        byte[] bytes = classWriter.toByteArray();

        ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader();
        Class<?> clazz = byteArrayClassLoader.defineClazz(bytes);
        System.out.println(clazz.getDeclaredField("name").get(clazz.newInstance()));
    }
}


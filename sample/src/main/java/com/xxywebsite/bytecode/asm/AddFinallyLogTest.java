package com.xxywebsite.bytecode.asm;


import org.objectweb.asm.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import static net.bytebuddy.jar.asm.Opcodes.*;

public class AddFinallyLogTest {
    public static class MathUtil {
        public static int add(int num1, int num2) {
            return num1 + num2;
        }
    }

    public static void main(String[] args) throws Exception {
        ClassReader classReader = new ClassReader("com.xxywebsite.bytecode.asm.AddFinallyLogTest$MathUtil");
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM8, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                if ("add".equals(name)) {
                    return new MethodVisitor(Opcodes.ASM8, methodVisitor) {
                        private Label startLabel = new Label();
                        private Label endLabel = new Label();
                        private Label finallyOrCacheLabel = new Label();

                        @Override
                        public void visitCode() {
                            methodVisitor.visitTryCatchBlock(startLabel, endLabel, finallyOrCacheLabel, null);
                            methodVisitor.visitLabel(startLabel);
                            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                            methodVisitor.visitLdcInsn("start");
                            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                        }

                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == IRETURN) {
                                methodVisitor.visitVarInsn(ISTORE, 2);
                                methodVisitor.visitLabel(endLabel);
                                finallyStatement(methodVisitor);
                                methodVisitor.visitVarInsn(ILOAD, 2);
                                methodVisitor.visitInsn(IRETURN);
                                methodVisitor.visitLabel(finallyOrCacheLabel);
                                methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
                                methodVisitor.visitVarInsn(ASTORE, 3);
                                finallyStatement(methodVisitor);
                                methodVisitor.visitVarInsn(ALOAD, 3);
                                methodVisitor.visitInsn(ATHROW);
                            } else {
                                super.visitInsn(opcode);
                            }
                        }

                        private void finallyStatement(MethodVisitor methodVisitor) {
                            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                            methodVisitor.visitLdcInsn("num1 = %d, num2 = %d, \u4e24\u6570\u4e4b\u548c\u4e3a:%d");

                            // 创建一个长度为3的Object数组
                            methodVisitor.visitInsn(ICONST_3);
                            methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                            methodVisitor.visitInsn(DUP);
                            methodVisitor.visitInsn(ICONST_0);
                            methodVisitor.visitVarInsn(ILOAD, 0);
                            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                            methodVisitor.visitInsn(AASTORE);
                            methodVisitor.visitInsn(DUP);
                            methodVisitor.visitInsn(ICONST_1);
                            methodVisitor.visitVarInsn(ILOAD, 1);
                            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                            methodVisitor.visitInsn(AASTORE);
                            methodVisitor.visitInsn(DUP);
                            methodVisitor.visitInsn(ICONST_2);
                            methodVisitor.visitVarInsn(ILOAD, 0);
                            methodVisitor.visitVarInsn(ILOAD, 1);
                            methodVisitor.visitInsn(IADD);
                            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                            methodVisitor.visitInsn(AASTORE);
                            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/String", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", false);
                            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                        }
                    };

                }
                return methodVisitor;
            }
        };
        classReader.accept(classVisitor, 0);
        byte[] bytes = classWriter.toByteArray();

        // ...
        Files.write(Paths.get("MathUtil.class"), bytes);
    }
}

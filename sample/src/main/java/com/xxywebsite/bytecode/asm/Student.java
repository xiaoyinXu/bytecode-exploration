package com.xxywebsite.bytecode.asm;

/**
 * 仅仅用于生成ASM代码
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class Student {
    private String name;

    private Integer age;

    public Student() {
    }

    public Student(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}


/**
 * package asm.com.xxywebsite.bytecode.asm;
 *
 * import org.objectweb.asm.AnnotationVisitor;
 * import org.objectweb.asm.Attribute;
 * import org.objectweb.asm.ClassReader;
 * import org.objectweb.asm.ClassWriter;
 * import org.objectweb.asm.ConstantDynamic;
 * import org.objectweb.asm.FieldVisitor;
 * import org.objectweb.asm.Handle;
 * import org.objectweb.asm.Label;
 * import org.objectweb.asm.MethodVisitor;
 * import org.objectweb.asm.Opcodes;
 * import org.objectweb.asm.Type;
 * import org.objectweb.asm.TypePath;
 *
 * public class StudentDump implements Opcodes {
 *
 *     public static byte[] dump() throws Exception {
 *
 *         ClassWriter classWriter = new ClassWriter(0);
 *         FieldVisitor fieldVisitor;
 *         MethodVisitor methodVisitor;
 *         AnnotationVisitor annotationVisitor0;
 *
 *         classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "com/xxywebsite/bytecode/asm/Student", null, "java/lang/Object", null);
 *
 *         classWriter.visitSource("Student.java", null);
 *
 *         {
 *             fieldVisitor = classWriter.visitField(ACC_PRIVATE, "name", "Ljava/lang/String;", null, null);
 *             fieldVisitor.visitEnd();
 *         }
 *         {
 *             fieldVisitor = classWriter.visitField(ACC_PRIVATE, "age", "Ljava/lang/Integer;", null, null);
 *             fieldVisitor.visitEnd();
 *         }
 *         {
 *             methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
 *             methodVisitor.visitCode();
 *             Label label0 = new Label();
 *             methodVisitor.visitLabel(label0);
 *             methodVisitor.visitLineNumber(12, label0);
 *             methodVisitor.visitVarInsn(ALOAD, 0);
 *             methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
 *             Label label1 = new Label();
 *             methodVisitor.visitLabel(label1);
 *             methodVisitor.visitLineNumber(13, label1);
 *             methodVisitor.visitInsn(RETURN);
 *             Label label2 = new Label();
 *             methodVisitor.visitLabel(label2);
 *             methodVisitor.visitLocalVariable("this", "Lcom/xxywebsite/bytecode/asm/Student;", null, label0, label2, 0);
 *             methodVisitor.visitMaxs(1, 1);
 *             methodVisitor.visitEnd();
 *         }
 *         {
 *             methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/String;Ljava/lang/Integer;)V", null, null);
 *             methodVisitor.visitCode();
 *             Label label0 = new Label();
 *             methodVisitor.visitLabel(label0);
 *             methodVisitor.visitLineNumber(15, label0);
 *             methodVisitor.visitVarInsn(ALOAD, 0);
 *             methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
 *             Label label1 = new Label();
 *             methodVisitor.visitLabel(label1);
 *             methodVisitor.visitLineNumber(16, label1);
 *             methodVisitor.visitVarInsn(ALOAD, 0);
 *             methodVisitor.visitVarInsn(ALOAD, 1);
 *             methodVisitor.visitFieldInsn(PUTFIELD, "com/xxywebsite/bytecode/asm/Student", "name", "Ljava/lang/String;");
 *             Label label2 = new Label();
 *             methodVisitor.visitLabel(label2);
 *             methodVisitor.visitLineNumber(17, label2);
 *             methodVisitor.visitVarInsn(ALOAD, 0);
 *             methodVisitor.visitVarInsn(ALOAD, 2);
 *             methodVisitor.visitFieldInsn(PUTFIELD, "com/xxywebsite/bytecode/asm/Student", "age", "Ljava/lang/Integer;");
 *             Label label3 = new Label();
 *             methodVisitor.visitLabel(label3);
 *             methodVisitor.visitLineNumber(18, label3);
 *             methodVisitor.visitInsn(RETURN);
 *             Label label4 = new Label();
 *             methodVisitor.visitLabel(label4);
 *             methodVisitor.visitLocalVariable("this", "Lcom/xxywebsite/bytecode/asm/Student;", null, label0, label4, 0);
 *             methodVisitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label4, 1);
 *             methodVisitor.visitLocalVariable("age", "Ljava/lang/Integer;", null, label0, label4, 2);
 *             methodVisitor.visitMaxs(2, 3);
 *             methodVisitor.visitEnd();
 *         }
 *         {
 *             methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getName", "()Ljava/lang/String;", null, null);
 *             methodVisitor.visitCode();
 *             Label label0 = new Label();
 *             methodVisitor.visitLabel(label0);
 *             methodVisitor.visitLineNumber(21, label0);
 *             methodVisitor.visitVarInsn(ALOAD, 0);
 *             methodVisitor.visitFieldInsn(GETFIELD, "com/xxywebsite/bytecode/asm/Student", "name", "Ljava/lang/String;");
 *             methodVisitor.visitInsn(ARETURN);
 *             Label label1 = new Label();
 *             methodVisitor.visitLabel(label1);
 *             methodVisitor.visitLocalVariable("this", "Lcom/xxywebsite/bytecode/asm/Student;", null, label0, label1, 0);
 *             methodVisitor.visitMaxs(1, 1);
 *             methodVisitor.visitEnd();
 *         }
 *         {
 *             methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "setName", "(Ljava/lang/String;)V", null, null);
 *             methodVisitor.visitCode();
 *             Label label0 = new Label();
 *             methodVisitor.visitLabel(label0);
 *             methodVisitor.visitLineNumber(25, label0);
 *             methodVisitor.visitVarInsn(ALOAD, 0);
 *             methodVisitor.visitVarInsn(ALOAD, 1);
 *             methodVisitor.visitFieldInsn(PUTFIELD, "com/xxywebsite/bytecode/asm/Student", "name", "Ljava/lang/String;");
 *             Label label1 = new Label();
 *             methodVisitor.visitLabel(label1);
 *             methodVisitor.visitLineNumber(26, label1);
 *             methodVisitor.visitInsn(RETURN);
 *             Label label2 = new Label();
 *             methodVisitor.visitLabel(label2);
 *             methodVisitor.visitLocalVariable("this", "Lcom/xxywebsite/bytecode/asm/Student;", null, label0, label2, 0);
 *             methodVisitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label2, 1);
 *             methodVisitor.visitMaxs(2, 2);
 *             methodVisitor.visitEnd();
 *         }
 *         {
 *             methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getAge", "()Ljava/lang/Integer;", null, null);
 *             methodVisitor.visitCode();
 *             Label label0 = new Label();
 *             methodVisitor.visitLabel(label0);
 *             methodVisitor.visitLineNumber(29, label0);
 *             methodVisitor.visitVarInsn(ALOAD, 0);
 *             methodVisitor.visitFieldInsn(GETFIELD, "com/xxywebsite/bytecode/asm/Student", "age", "Ljava/lang/Integer;");
 *             methodVisitor.visitInsn(ARETURN);
 *             Label label1 = new Label();
 *             methodVisitor.visitLabel(label1);
 *             methodVisitor.visitLocalVariable("this", "Lcom/xxywebsite/bytecode/asm/Student;", null, label0, label1, 0);
 *             methodVisitor.visitMaxs(1, 1);
 *             methodVisitor.visitEnd();
 *         }
 *         {
 *             methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "setAge", "(Ljava/lang/Integer;)V", null, null);
 *             methodVisitor.visitCode();
 *             Label label0 = new Label();
 *             methodVisitor.visitLabel(label0);
 *             methodVisitor.visitLineNumber(33, label0);
 *             methodVisitor.visitVarInsn(ALOAD, 0);
 *             methodVisitor.visitVarInsn(ALOAD, 1);
 *             methodVisitor.visitFieldInsn(PUTFIELD, "com/xxywebsite/bytecode/asm/Student", "age", "Ljava/lang/Integer;");
 *             Label label1 = new Label();
 *             methodVisitor.visitLabel(label1);
 *             methodVisitor.visitLineNumber(34, label1);
 *             methodVisitor.visitInsn(RETURN);
 *             Label label2 = new Label();
 *             methodVisitor.visitLabel(label2);
 *             methodVisitor.visitLocalVariable("this", "Lcom/xxywebsite/bytecode/asm/Student;", null, label0, label2, 0);
 *             methodVisitor.visitLocalVariable("age", "Ljava/lang/Integer;", null, label0, label2, 1);
 *             methodVisitor.visitMaxs(2, 2);
 *             methodVisitor.visitEnd();
 *         }
 *         classWriter.visitEnd();
 *
 *         return classWriter.toByteArray();
 *     }
 * }
 */
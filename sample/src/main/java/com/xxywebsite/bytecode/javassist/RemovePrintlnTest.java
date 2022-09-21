package com.xxywebsite.bytecode.javassist;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class RemovePrintlnTest {
    public static class MathOperator {
        public int add(int num1, int num2) {
            try {
                return num1 + num2;
            } finally {
                System.out.println(String.format("num1 = %d, num2 = %d, 两数之和为%d", num1, num2, num1 + num2));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.get("com.xxywebsite.bytecode.javassist.RemovePrintlnTest$MathOperator");
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            ctMethod.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if ("println".equals(m.getMethodName())) {
                        m.replace("{}");
                    }
                }
            });
        }
        Class<?> clazz = ctClass.toClass();
        MathOperator mathOperator = (MathOperator) clazz.newInstance();
        System.out.println(mathOperator.add(2, 3));;
    }
}

package com.xxywebsite.bytecode.javassist;

import com.xxywebsite.bytecode.common.HelloService;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class JavassistProxyTest {
    public static void main(String[] args) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.get("com.xxywebsite.bytecode.common.HelloServiceImpl");
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            ctMethod.insertBefore(String.format("System.out.println(\"开始执行:%s方法\");", ctMethod.getName()));
            ctMethod.insertAfter(String.format("System.out.println(\"结束执行:%s方法\");", ctMethod.getName()));
        }
        Class<?> clazz = ctClass.toClass();
        HelloService helloService = (HelloService) clazz.newInstance();
        helloService.sayHello("Javassist");
    }
}

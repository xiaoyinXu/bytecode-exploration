package com.xxywebsite.bytecode.javassist;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class MakeClassTest {
    public static void main(String[] args) throws Exception {
        ClassPool classPool = ClassPool.getDefault();

        // 用到java.util.List, 需要手动导包
        classPool.importPackage("java.util");


        CtClass ctClass = classPool.makeClass("ListUtil");
        ClassFile classFile = ctClass.getClassFile();
        ConstPool constPool = classFile.getConstPool();


        // 增加注解
        AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation annotation = new Annotation("java.lang.Deprecated", constPool);
        annotationsAttribute.setAnnotation(annotation);
        classFile.addAttribute(annotationsAttribute);

        // 增加接口
        CtClass serializableCtClass = classPool.get("java.io.Serializable");
        ctClass.addInterface(serializableCtClass);

        // 增加字段
        ctClass.addField(CtField.make("private String field1;", ctClass));
        ctClass.addField(CtField.make("private Integer field2;", ctClass));


        // 增加方法
        // 内部编译器只支持到JDK1.5，不支持泛型
        ctClass.addMethod(
                CtMethod.make(
                        "public static List newArray() {return new ArrayList();}"
                        , ctClass));

        ctClass.addMethod(
                CtMethod.make(
                        "public static boolean isEmpty(List list) {return list == null || list.isEmpty();}"
                        , ctClass));

        ctClass.writeFile(".");
    }
}

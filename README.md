# Java Bytecode Manipulation Exploration

## 什么是Bytecode Manipulation？

字节码是JVM平台语言（如Java、Kotlin、Scala、groovy）的概念，本文以Java为例。Java源文件在被编译的时候，并不会直接被编译成机器代码，而会被编译成以.class后缀的字节码文件并存储到硬盘上，只有程序真正执行的时候，".class"文件才会被翻译成与操作系统、处理器架构适配的可执行机器代码，而".class"文件存储的内容其实就是字节码。

.class文件是一种"二进制文件"，它按照顺序记录了很多信息，如字节码版本、常量池、类的各种元信息，如类名、父类、接口、实例属性、实例方法、静态属性、静态方法、注解等，本质上它就是一个byte数组，如果我们能按照字节码规范去创建/修改一个已存在的byte数组，那我们就可以做到创建类以及修改类的各种行为。

然而去造一个byte数组是一件很困难的事，而bytecode manipulation则是辅助我们去构建这个"byte数组"的技术。

#### 字节码的"各种形式"

// TODO 截图...

#### 字节码在各种框架的应用

// TODO 举例...

## 为什么需要Bytecode Manipulation?

先给出结论: TODO

在讨论为什么需要字节码增强技术之前，我们先谈谈Java语言本身。Java是一门静态类型语言，静态类型语言要求变量的类型需要显式指定且在编译期间是可知的，即当声明了变量后，其类型是无法修改的。

```java
public class Main {
    public void main(String[] args) {
        int x = 5;
        double y = 6.0;
        x = y;  // 编译报错
    }
}
```

而相较于静态类型语言，动态类型语言不用声明变量类型，变量类型在运行时才确定，即在编译期间变量可以指向各种"对象"，整体代码风格会非常简约, 如Python
```python
if __name__ == "__main__":
    x = 12000  
    print(type(x))  # <class 'int'>
      
    x = 'Dynamic Typing'  
    print(type(x))  # <class 'str'>

      
    x = [1, 2, 3, 4]  
    print(type(x))  # <class 'list'>

```

然而动态类型语言的`变量类型在运行时才确定`也有一个明显的缺点：有很多错误或异常运行时才能被发现。

```python
class Student:
    def __init__(self, name, age):
        self.name = name
        self.age = age

if __name__ == "__main__":
    student = Student("Cookie", 24)
    print_info(student)
    print(student.name)
    print(student.age)
    print(student.gender) # AttributeError: 'student' object has no attribute 'gender'
```

但很多时候，尤其是在实现通用框架的时候，"框架"是无法知道"用户"使用哪些类型的，那框架怎么在不知道用户类型的情况下去调用用户方法呢？对于Python这种动态类型语言，由于在编码期间IDE无法推断变量类型，如下代码是不会报错的, 用户只需要在使用的时候传入实现了execute_sql的方法就行。

```python
class DriverProxy:
   def __init__(self, driver):
      self.driver = driver
   def execute_sql(sql):
      self.driver.execute_sql(sql) # 不会报错
    
 
```

对于Java这门静态类型语言来说，如果不确定变量的具体类型，是无法调用相应的方法的。那在Java里，框架一般如何去调用用户代码呢？介绍两种方式
（便于说明，这里仅仅用一个静态代理来代表"第三方框架"）

`方式1 （接口）`

```java

// 框架代码
public interface Driver {
    void executeSql(String sql);
}

public class DriverProxy {
    private Driver driver;

    public DriverProxy(Driver driver) {
        this.driver = driver;
    }

    public executeSql(String sql) {
        this.driver.executeSql(sql);
    }
}

// 用户代码
public class MyDriver implements Driver {
    public void executeSql(String sql) {
        // logic
    }
}

public class Main {
    public static void main(String[] args) {
        Driver driver = new MyDriver();
        driver.executeSql("...");
    }
}
```

方式1借助接口，框架不用关心用户传入的变量具体是什么类型，只要用户传入任意一个具体的实现类，框架在运行时就能调用到具体用户代码啦。当然了这也得益于面向对象语言多态特性。

`方式2 反射`

```java
import java.lang.reflect.Method;

// 框架代码
@Retention(RetentionPolicy.RUNTIME)
public @interface Secured {
    String role();
}

public class SecureMethodUtil {
    public static void invokeMethod(Method method, Object instance, Object... args) {
        if (method.isAnnotationPresent(Secured.class)) {
            Secured secured = method.getAnnotation(Secured.class);
            String role = secured.role();
            CurrentUser user = ... //
            if (!use.hasRole(role)) {
                throw new RuntimeException(String.format("当前用户没有%s方法执行权限, 缺少:%s角色", method.getName(), role));
            }
        }

        // 执行原方法
        method.invoke(instance, args);
    }
}


// 用户代码
public class RiskInvoker {
    @Secured(role = "admin")
    public void deleteAllRecords(String tableName) {
        // logic
    }

    @Secured(role = "root")
    public void dropTable(String tableName) {
        // logic
    }
}

public class Main {
    public static void main(String[] args) {
        // 获取反射方法
        Method method = ...;
        SecureMethodUtil.invokeMethod(method, arg1, arg2, ...);
    }
}
```

如上例，SecureMethodUtil用于执行用户代码前，校验当前用户是否有执行权限。"框架"不用在编译时关心用户类型是什么，借助反射API，框架也能在运行时执行用户代码。

反射是Java语言很强大的特性，它提供了运行时获取类的各种元数据的能力（得益于字节码),它在框架里经常被使用。例如fastJson、easyExcel，它们就是动态去获取当前对象类的所有实例属性的getter/setter方法，这样在类去拓展/修改字段时，相应用户代码不用做任何调整就能实现json字符串、excel文件的变化。

反射为Java这门静态类型语言提供了一定的动态特性，但也存在一些问题。

1. 第一个是性能问题。通过class对象去获取方法(Method)和属性(Field)的开销是很大的，但一旦缓存了Method、Field对象，之后对方法/属性的访问会快很多。
   尽管在执行方式时会调用native方法(sun.reflect.NativeMethodAccessorImpl#invoke0),
   部分native方法的调用会比Java方法慢，而JVM也对此做了优化，所以在很多场景中，反射不会出现性能瓶颈，感兴趣的可以看看[java relfection inflation]()

2. 大量使用反射，在项目工程里无法很快发现一些属性和方法的依赖关系。如果属性和方法是显式调用的，那么很容易借助IDE知道这些属性或方法被哪些地方依赖了，但一旦用了反射，IDE就束手无策了。
   
3. 反射抛弃了Java静态语言类型的优势，即编译时类型检查。借助反射去执行一个方法时，method.invoke(...)的传参是否正确只有在运行时才能发现，"框架"显然不希望暴露给"用户"反射API,
   因为这会增加很多不确定性。

当然以上问题不能遮盖反射在各种场景发挥的巨大作用，字节码增强技术往往也会搭配反射API使用。

`接下来进入正题---Bytecode Manipulation`

Bytecode Manipulation让用户能够去操作字节码，进而能够控制类的各种行为。

## 常用Bytecode Manipulation框架简单介绍

当然，目前Bytecode Manipulation最常用的一个场景还是动态代理，动态代理可以增强用户定义的方法，比如增加日志、缓存逻辑、事务逻辑等。
首先我们定义一个简单的接口和实现

```java
public interface HelloService {
    void sayHello(String name);
}

public class HelloServiceImpl implements HelloService {
    @Override
    public void sayHello(String name) {
        System.out.println(String.format("Hello %s", name));
    }
}
```

### jdk proxy
jdk proxy即我们常说的JDK动态代理，实际上它是jdk内置的工具类，借助它我们能快速地实现动态代理。

```java
public class JdkProxyTest {
    public static void main(String[] args) {
        HelloService helloService = (HelloService) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class<?>[]{HelloService.class},
                new LogInvocationHandler(new HelloServiceImpl()));
        helloService.sayHello("JdkProxy");
    }

    private static class LogInvocationHandler implements InvocationHandler {
        private Object instance;

        public LogInvocationHandler(Object instance) {
            this.instance = instance;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            long startTs = System.currentTimeMillis();
            try {
                return method.invoke(instance, args);
            } finally {
                System.out.println(String.format("方法:%s, 共耗时%dms", method.getName(), System.currentTimeMillis() - startTs));
            }
        }
    }
}
```

然而JDK动态代理的缺点也很明显，它只能代理实现了接口的类，且无法细粒度选择拦截哪些方法，

### javassist

javassist是一个使用起来相对比较简单的生成字节码的工具，它使用起来比较贴近Java的反射API。
它使用自己实现的一个Java编译器（不支持泛型），在调用一些创建/修改方法的API时，需要将java源码作为字符串入参，在编写一些复杂定制逻辑时还是很容易出错。
建议作为学习使用。

####动态代理

```java
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
```

#### 去除所有println

```java
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
                    // 去除方法体内的System.out.println
                    if ("println".equals(m.getMethodName())) {
                        m.replace("{}");
                    }
                }
            });
        }
        Class<?> clazz = ctClass.toClass();
        MathOperator mathOperator = (MathOperator) clazz.newInstance();
        System.out.println(mathOperator.add(2, 3));
    }
}

```

#### 创建一个class

```java
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
```

生成的class文件反编译后如下

```java
// 以上生成的类如下
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class ListUtil implements Serializable {
    private String field1;
    private Integer field2;

    public static List newArray() {
        return new ArrayList();
    }

    public static boolean isEmpty(List var0) {
        return var0 == null || var0.isEmpty();
    }

    public ListUtil() {
    }
}

```

### asm

asm是一个专门致力于bytecode manipulation的框架，很多优秀的框架都使用了它，例如cglib、byte-buddy实质就是对asm的封装，就连
JDK本身也将asm打包到了自己的命名空间里。 TODO 如图
所以在使用ASM的时候一定要注意不要import错包了。

asm基于观察者设计模式设计了一套Visitor API, 核心由ClassReader(读取已有类)、ClassVisitor(修改类的各种行为)、ClassWriter(输出结果)。
其使用起来比较偏底层，先来看看用ASM完全从零创建一个类的画风。例如我们想要实现以下这个POJO类

```java
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
```

如果要用ASM visitor api实现，代码如下（借助Intellij Idea插件 ASM Bytecode Viewer生成）

```java
package asm.com.xxywebsite.bytecode.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

public class StudentDump implements Opcodes {

    public static byte[] dump() throws Exception {

        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;
        AnnotationVisitor annotationVisitor0;

        classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "com/xxywebsite/bytecode/asm/Student", null, "java/lang/Object", null);

        classWriter.visitSource("Student.java", null);

        {
            fieldVisitor = classWriter.visitField(ACC_PRIVATE, "name", "Ljava/lang/String;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            fieldVisitor = classWriter.visitField(ACC_PRIVATE, "age", "Ljava/lang/Integer;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(12, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(13, label1);
            methodVisitor.visitInsn(RETURN);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLocalVariable("this", "Lcom/xxywebsite/bytecode/asm/Student;", null, label0, label2, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/String;Ljava/lang/Integer;)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(15, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(16, label1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, "com/xxywebsite/bytecode/asm/Student", "name", "Ljava/lang/String;");
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(17, label2);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitFieldInsn(PUTFIELD, "com/xxywebsite/bytecode/asm/Student", "age", "Ljava/lang/Integer;");
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLineNumber(18, label3);
            methodVisitor.visitInsn(RETURN);
            Label label4 = new Label();
            methodVisitor.visitLabel(label4);
            methodVisitor.visitLocalVariable("this", "Lcom/xxywebsite/bytecode/asm/Student;", null, label0, label4, 0);
            methodVisitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label4, 1);
            methodVisitor.visitLocalVariable("age", "Ljava/lang/Integer;", null, label0, label4, 2);
            methodVisitor.visitMaxs(2, 3);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getName", "()Ljava/lang/String;", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(21, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "com/xxywebsite/bytecode/asm/Student", "name", "Ljava/lang/String;");
            methodVisitor.visitInsn(ARETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "Lcom/xxywebsite/bytecode/asm/Student;", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "setName", "(Ljava/lang/String;)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(25, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, "com/xxywebsite/bytecode/asm/Student", "name", "Ljava/lang/String;");
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(26, label1);
            methodVisitor.visitInsn(RETURN);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLocalVariable("this", "Lcom/xxywebsite/bytecode/asm/Student;", null, label0, label2, 0);
            methodVisitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label2, 1);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getAge", "()Ljava/lang/Integer;", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(29, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "com/xxywebsite/bytecode/asm/Student", "age", "Ljava/lang/Integer;");
            methodVisitor.visitInsn(ARETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "Lcom/xxywebsite/bytecode/asm/Student;", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "setAge", "(Ljava/lang/Integer;)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(33, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, "com/xxywebsite/bytecode/asm/Student", "age", "Ljava/lang/Integer;");
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(34, label1);
            methodVisitor.visitInsn(RETURN);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLocalVariable("this", "Lcom/xxywebsite/bytecode/asm/Student;", null, label0, label2, 0);
            methodVisitor.visitLocalVariable("age", "Ljava/lang/Integer;", null, label0, label2, 1);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}

```

从上面的例子可以看出，使用ASM visitor api就像在手动书写".class"字节码文件一样，这里面的每一个API基本都能找到对应的操作码
TODO 如图

你需要按照一定顺序去调用各个API，如果顺序出现问题则会"编写"出不合法的class文件，所以这对不了解class文件以及JVM底层执行方法逻辑的
小伙伴不是很友好。asm提供出来的api实在是太琐碎了，但基于观察者这种设计模式，当我们仅仅是想对已有类的部分模块进行修改时，则只需要重载相应的方法，当你熟悉了这种模式后，你就会发现ASM相较于其它bytecode manipulation能够完成更多细腻的功能。

#### 使用模板

```java
public class Main {
    public static void main(String[] args) throws Exception {
        ClassReader classReader = new ClassReader("..."); // class全限定名
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9, classWriter) {
            // override 各种方法
        };
        classReader.accept(classVisitor, 0);
        byte[] classBytes = classWriter.toByteArray(); // 得到"字节码流"

        // ... 后续操作
    }
}
```

例如我们想要去掉类的一个注解

#### 删除/增加注解

`删除注解`

```java
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
    }
}

```

`增加注解`

```java
public class AddClassAnnotationTest {
    public static class A {

    }

    public static void main(String[] args) throws Exception {
        ClassReader classReader = new ClassReader("com.xxywebsite.bytecode.asm.AddClassAnnotationTest$A");
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9, classWriter) {
            @Override
            public void visitEnd() {
                super.visitAnnotation("Ljava/lang/Deprecated;", true);
            }
        };
        classReader.accept(classVisitor, 0);
        byte[] classBytes = classWriter.toByteArray(); // 已经去掉了@Deprecated后的字节码

        // 测试
        ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader();
        Class<?> clazz = byteArrayClassLoader.defineClazz(classBytes);
        System.out.println(clazz.isAnnotationPresent(Deprecated.class));
    }
}
```

#### 修改实例属性的access flag

```java
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
```

#### 打印日志

只要不涉及到方法体，asm使用起来还不是很复杂。但是如果涉及到方法体、尤其是增加一些try-catch、try-finally逻辑时，用asm实现起来就很痛苦。
例如我们想为以下方法增加一个日志

```java
public class MathUtil {
    public static int add(int num1, int num2) {
        return num1 + num2;
    }
}
```

如果我们想将其变成

```java
public class MathUtil {
    public static int add(int num1, int num2) {
        try {
            System.out.println("start");
            return num1 + num2;
        } finally {
            System.out.println(String.format("num1 = %d, num2 = %d, 两数之和为:%d", num1, num2, num1 + num2));
        }
    }
}
```

其对应的asm实现如下
// TODO

```java
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
        Files.write(Paths.get("MathUtil.class"), bytes);

    }
}

```
由上例可以看出，尽管asm可以帮我们自动计算操作数栈的最大深度（maxStack）以及局部变量的个数(maxLocals), 但在实现方法逻辑的时候还是过于复杂了。
所以我建议涉及到方法体的修改或创建时，可以使用下面的两个框架cglib、byte-buddy。

### cglib

cglib是对ASM进行封装的一个框架，在Spring Framework里常常见到它的身影。但可惜的是它没有跟上Java本身的发展，它对JDK17及以上版本已经支持的
不是很好了，在[github仓库](https://github.com/cglib/cglib)已经很久没有维护了，它建议开发者在新的JDK版本里使用byte-buddy。
我们这里简单如何用它实现动态代理

```java
public class CglibProxyTest {
    public static class MyMethodInterceptor implements MethodInterceptor {
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            long startTs = System.currentTimeMillis();
            try {
                return proxy.invokeSuper(obj, args);
            } finally {
                System.out.println(String.format("方法:%s, 共耗时:%dms", method.getName(), System.currentTimeMillis() - startTs));
            }
        }
    }

    public static void main(String[] args) {
        HelloService helloService = (HelloService) Enhancer.create(HelloServiceImpl.class, new MyMethodInterceptor());
        helloService.sayHello("Cookie");
    }
}
```

### byte-buddy

接下来我们介绍一下byte-buddy这个框架，它实质也是对ASM框架的封装。相较于前面几个前辈，byte-buddy出身较晚，它2013年底才开源，且我从commit提交记录和社区问答
里发现这个项目至今几乎一直是[Rafael Winterhalter](https://github.com/raphw) 一个人在维护。但是从我最近对byte-buddy api的学习，
它的api使用体验是比较好的，更重要的是它暴露出了部分ASM visitor api（尽管作者不建议我们使用），这使得byte-buddy的功能性也变得很强大。
废话不多说，我们又又又又双叒叕来实现一个动态代理

```java
public class ByteBuddyProxyTest {
    public static class LogImplementation {
        @RuntimeType
        public static Object invoke(@Origin Method method, @SuperCall Callable<?> callable) {
            long startTs = System.currentTimeMillis();
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println(String.format("方法:%s, 耗时:%dms", method.getName(), System.currentTimeMillis() - startTs));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Class<? extends HelloServiceImpl> clazz = new ByteBuddy()
                .subclass(HelloServiceImpl.class)
                .method(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                .intercept(MethodDelegation.to(LogImplementation.class))
                .make()
                .load(ClassLoader.getSystemClassLoader())
                .getLoaded();

        HelloService helloService = clazz.newInstance();
        helloService.sayHello("Cookie");
    }
}
```

第一次接触Byte-Buddy的小伙伴可能会觉得这个api使用起来非常啰嗦（习惯就好），但实际上这是为了能让用户能做更细腻度的控制。首先subclass代表你要继承哪个类或者实现哪个接口，之后method(...)用于筛选你想要进行拦截/重载的方法，以上就是过滤掉了Object声明的一些方法，紧接着intercept(...)就是方法的实现，这里最常用的api就是MethodDelegation(方法委托),它将原先的方法重载并委托给了其它类/对象。

我们重点看看这个LogImplementation这个类的实现，一般被委托的类会实现一个且仅一个方法，而最重要的地方就是方法、方法参数上的注解。@RuntimeType加在了方法上，代表这个方法的返回值可以是任意类型，假如不用这个注解，那么返回值必须和被委托方法(即sayHello(String))保持一致：void。而方法参数的注解@Origin, 指明当前method是被委托方法，而@SuperCall，指明当前callable包含了被委托方法的执行逻辑，我们只需要执行callable.call()就能触发被委托方法的逻辑。除了这些注解还有几个常用的注解, 例如`@This Object this`指明当前object是代理类实例的this指针，`@AllArguments Object[] args`指明args是方法的所有调用参数。一旦参数上加上了这些注解，用byte-buddy操作字节码生成的类在调用被委托方法前，会将这些被注解的参数按照语义赋值后，再调用LogImplementation.invoke方法,还有其它注解这里就不一一举例了。

## java agent

### java agent是什么

java agent是java很早就有的一个概念，运用字节码增强技术时一般都会和java agent搭配使用。那什么是java agent呢？

我的理解是它是一个类加载的网关：当类被加载到类加载器之前，java agent可以对已经读取的字节码流进行修改，从而来控制类的各种行为。既然它是一个网关，那我们就可以对每一个加载到JVM里的类（当然，部分jdk的类会在使用java agent前初始化）进行统一控制，而不用像上面所有例子一样针对每一个类都要手动去编写一大段代码,这里我们先看一下java agent如何使用, 它有两种使用方式，一种是是可插拔式的静态使用方式(通过启动时指定jvm参数)，一种是利用java的attach api的动态使用方式（动态attach到已经启动的JVM进程）

### 静态使用方式

`创建一个类，像main方法一样写一个固定的premain方法`

```java
public class JavaAgentTemplate {
    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(
                new ClassFileTransformer() {
                    @Override
                    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                        // 仅仅打印每一个被加载的类
                        System.out.println(String.format("%s被加载", className));

                        return classfileBuffer;
                    }
                }
        );
    }
}
```

然后创建META-INF/MANIFEST.MF, 内容如下（替换成相应的全路径名）

```
Premain-Class: com.xxywebsite.bytecode.common.JavaAgentTemplate
```

为了将MANIFEST.MF打包到jar包里，pom.xml里增加一个maven插件, 这里我选择maven-jar-plugin

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <version>3.2.0</version>
            <configuration>
                <archive>
                    <manifestFile>src/main/resources/META-INF/MANIFEST.MF</manifestFile>
                </archive>
            </configuration>
        </plugin>
    </plugins>
</build>
```

打包成jar包，记住jar包的路径，在你启动JVM进程前增加JMV参数(替换成自己的路径)

```
java -javaagent /Users/xuxiaoyin/Projects/bytecode-exploration/sample/target/bytecode-manipulation-sample-1.0-SNAPSHOT.jar -cp ${YourClassPath} ${YourMainClass}
```

这样启动JVM后就会打印每一个被加载的类啦。

由上例看出, ClassFileTransformer是使用java agent的关键，那byte buddy也提供了简易的agent API, 下面我们用byte-buddy-agent去为每一个方法调用都增加日志。

```java
public class MyByteBuddyLogAgent {
    public static class LogImplementation {
        @RuntimeType
        public static Object invoke(@Origin Method method, @SuperCall Callable<?> callable) {
            long startTs = System.currentTimeMillis();
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println(String.format("执行:%s方法，耗时:%dms", method.getName(), System.currentTimeMillis() - startTs));
            }
        }
    }

    public static void premain(String args, Instrumentation instrumentation) {
        new AgentBuilder.Default()
                .type(ElementMatchers.nameStartsWith("com.xxywebsite"))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(ElementMatchers.any())
                                .intercept(MethodDelegation.to(LogImplementation.class))
                ).installOn(instrumentation);
    }
}
```

以上和我们用byte-buddy实现动态代理很像，但值得注意的是通过java agent增强的类在classloader看来还是原先的类，并非当成代理。

### 动态使用方式
首先介绍Attach API
#### Attach API
JVM提供了attach api, 其实我们经常使用的jstack、jmap等命令实际都借助了它，简单来看看它是啥。
```java
public class AttachApiTest {
    public static void main(String[] args) throws Exception {
        // 模拟jps命令
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        for (VirtualMachineDescriptor virtualMachineDescriptor : list) {
            String id = virtualMachineDescriptor.id();
            String name = virtualMachineDescriptor.displayName();
            System.out.println(String.format("%s %s", id, name));
        }

        // 模拟jstack
        VirtualMachine virtualMachine = VirtualMachine.attach("17729"); // 替换pid
        HotSpotVirtualMachine hotSpotVirtualMachine = (HotSpotVirtualMachine) virtualMachine;
        InputStream inputStream = hotSpotVirtualMachine.remoteDataDump();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        bufferedReader.lines().forEach(System.out::println);

        // 模拟jmap -histo:live
        inputStream = hotSpotVirtualMachine.heapHisto();
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        bufferedReader.lines().forEach(System.out::println);

        // 动态使用java agent, 无返回值
        hotSpotVirtualMachine.loadAgent("{YourPath}/{your-agent}.jar");
    }
}
```
简单来说, attach api可以与一个正在运行的JVM进程进行通信(会起一个Attach Listener线程)，而loadAgent方法则用来动态使用java agent。
一旦调用了这个api, 被attach的jvm进程会在Attach Listener线程里去执行对应agent jar的agentMain方法，它与premain一样，格式如下。
```java
public class JavaAgentTemplate {
    public static void agentmain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(
                new ClassFileTransformer() {
                    @Override
                    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                        // Your Logic 
                        // ...

                        return classfileBuffer;
                    }
                }
        );
    }
}
```
执行完agentmain方法之后，之后的类在被加载到类加载器前都会被ClassFileTransformer拦截， 其它地方与静态使用方式(-javaagent)没有太大差异。

### 两种方式对比
对比下来，Java Agent的静态使用方式可以从jvm刚开始运行时就去管控类的加载，在生产环境里经常使用这种方式，然而它的"缺点"就是使用起来太麻烦：需要指定MANIFEST.MF, 且需要增加JVM参数(-javaagent) 。动态使用方式需要调用attach api, 且也需要指定agent jar的路径，但是ByteBuddy已经为我们封装好了这个步骤，只需要调用ByteBuddyAgent.install(), 就能返回关键的instrumentation实例，使用起来相对静态方式会简单很多

```java
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Phaser;

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


      // 后续类的加载都会被拦截
      new ArrayBlockingQueue<Integer>(10);  // 打印 java/util/concurrent/ArrayBlockingQueue被加载
   }
}
```

#### Java Agent的"妙用"
Java Attach API只暴露了有限的方法，而loadAgent可以让我们执行指定jar包的agentmain方法，除了增加ClassFileTransformer外，实际上你可以在agentmain
执行任意逻辑，例如像[arthas](https://github.com/alibaba/arthas)一样，启动很多监听线程，后续可以直接与外界进行进程通信。
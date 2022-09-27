# Java Bytecode Manipulation Exploration

* [什么是动态字节码技术](#什么是动态字节码技术)
* [为什么需要动态字节码技术](#为什么需要动态字节码技术)
* [常用动态字节码技术介绍](#常用动态字节码技术介绍)
 * [jdk proxy](#jdk-proxy)
 * [javassist](#javassist)
 * [asm](#asm)
 * [cglib](#cglib)
 * [byte-buddy](#byte-buddy)
* [java agent](#java-agent)
 * [java agent是什么](#java-agent是什么)
 * [静态使用方式](#静态使用方式)
 * [动态使用方式](#动态使用方式)
    * [Java Attach API](#Java-Attach-API)
    * [Self Attaching](#Self-Attaching)
    * [重新加载类](#重新加载类)
 * [其它用法](#其它用法)




## 什么是动态字节码技术

字节码是JVM平台语言（如Java、Kotlin、Scala、groovy）的概念，本文以Java为例。javac编译器并不会将Java源文件直接编译成机器代码，而是编译成以.class为后缀的文件并持久化到硬盘上，当程序被执行的时候，".class文件"会被加载到内存里，而".class"文件存储的内容其实就是字节码。

如以下HelloWorld.java文件编译后的字节码文件为HelloWorld.class
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```
经过`java -p HelloWorld.class`后，解析结果如下
```sql
Classfile /Users/xuxiaoyin/Projects/bytecode-exploration/sample/target/classes/HelloWorld.class
  Last modified Sep 25, 2022; size 533 bytes
  MD5 checksum 3787d3eda484722c83dac067f2f78df2
  Compiled from "HelloWorld.java"
public class HelloWorld
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Methodref          #6.#20         // java/lang/Object."<init>":()V
   #2 = Fieldref           #21.#22        // java/lang/System.out:Ljava/io/PrintStream;
   #3 = String             #23            // Hello World
   #4 = Methodref          #24.#25        // java/io/PrintStream.println:(Ljava/lang/String;)V
   #5 = Class              #26            // HelloWorld
   #6 = Class              #27            // java/lang/Object
   #7 = Utf8               <init>
   #8 = Utf8               ()V
   #9 = Utf8               Code
  #10 = Utf8               LineNumberTable
  #11 = Utf8               LocalVariableTable
  #12 = Utf8               this
  #13 = Utf8               LHelloWorld;
  #14 = Utf8               main
  #15 = Utf8               ([Ljava/lang/String;)V
  #16 = Utf8               args
  #17 = Utf8               [Ljava/lang/String;
  #18 = Utf8               SourceFile
  #19 = Utf8               HelloWorld.java
  #20 = NameAndType        #7:#8          // "<init>":()V
  #21 = Class              #28            // java/lang/System
  #22 = NameAndType        #29:#30        // out:Ljava/io/PrintStream;
  #23 = Utf8               Hello World
  #24 = Class              #31            // java/io/PrintStream
  #25 = NameAndType        #32:#33        // println:(Ljava/lang/String;)V
  #26 = Utf8               HelloWorld
  #27 = Utf8               java/lang/Object
  #28 = Utf8               java/lang/System
  #29 = Utf8               out
  #30 = Utf8               Ljava/io/PrintStream;
  #31 = Utf8               java/io/PrintStream
  #32 = Utf8               println
  #33 = Utf8               (Ljava/lang/String;)V
{
  public HelloWorld();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 1: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   LHelloWorld;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=1, args_size=1
         0: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
         3: ldc           #3                  // String Hello World
         5: invokevirtual #4                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
         8: return
      LineNumberTable:
        line 3: 0
        line 4: 8
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       9     0  args   [Ljava/lang/String;
}

```
通过二进制文件编辑器打开HelloWorld.class, 解析结果如下
![](https://bj.bcebos.com/cookie/img.png)
通过Intellij IDEA jclasslib Bytecode Viewer插件，解析结果如下
![](https://bj.bcebos.com/cookie/img_1.png)
从上面的截图可以看出，字节码实际就是"class文件"的二进制数据。

对于程序最主要的组成部分---方法，实际也是一连串由操作码、操作数构成的二进制数据，解析结果如下
![](https://bj.bcebos.com/cookie/img_2.png))
红框里的getstatic、ldc、invokevirtual、return称之为操作码(OpCode), 后面的"参数"（根据操作码不同而不同）称之为操作数。

操作码由一个字节(8bit)构成，最多为256个操作码，目前大约就200多个操作码被用到，OpenJDK8的jdk.internal.org.objectweb.asm.Opcodes里定义了如下操作码
```
    int NOP = 0; // visitInsn
    int ACONST_NULL = 1; // -
    int ICONST_M1 = 2; // -
    int ICONST_0 = 3; // -
    int ICONST_1 = 4; // -
    int ICONST_2 = 5; // -
    int ICONST_3 = 6; // -
    int ICONST_4 = 7; // -
    int ICONST_5 = 8; // -
    int LCONST_0 = 9; // -
    int LCONST_1 = 10; // -
    int FCONST_0 = 11; // -
    int FCONST_1 = 12; // -
    int FCONST_2 = 13; // -
    int DCONST_0 = 14; // -
    int DCONST_1 = 15; // -
    int BIPUSH = 16; // visitIntInsn
    int SIPUSH = 17; // -
    int LDC = 18; // visitLdcInsn
    // int LDC_W = 19; // -
    // int LDC2_W = 20; // -
    int ILOAD = 21; // visitVarInsn
    int LLOAD = 22; // -
    int FLOAD = 23; // -
    int DLOAD = 24; // -
    int ALOAD = 25; // -
    // int ILOAD_0 = 26; // -
    // int ILOAD_1 = 27; // -
    // int ILOAD_2 = 28; // -
    // int ILOAD_3 = 29; // -
    // int LLOAD_0 = 30; // -
    // int LLOAD_1 = 31; // -
    // int LLOAD_2 = 32; // -
    // int LLOAD_3 = 33; // -
    // int FLOAD_0 = 34; // -
    // int FLOAD_1 = 35; // -
    // int FLOAD_2 = 36; // -
    // int FLOAD_3 = 37; // -
    // int DLOAD_0 = 38; // -
    // int DLOAD_1 = 39; // -
    // int DLOAD_2 = 40; // -
    // int DLOAD_3 = 41; // -
    // int ALOAD_0 = 42; // -
    // int ALOAD_1 = 43; // -
    // int ALOAD_2 = 44; // -
    // int ALOAD_3 = 45; // -
    int IALOAD = 46; // visitInsn
    int LALOAD = 47; // -
    int FALOAD = 48; // -
    int DALOAD = 49; // -
    int AALOAD = 50; // -
    int BALOAD = 51; // -
    int CALOAD = 52; // -
    int SALOAD = 53; // -
    int ISTORE = 54; // visitVarInsn
    int LSTORE = 55; // -
    int FSTORE = 56; // -
    int DSTORE = 57; // -
    int ASTORE = 58; // -
    // int ISTORE_0 = 59; // -
    // int ISTORE_1 = 60; // -
    // int ISTORE_2 = 61; // -
    // int ISTORE_3 = 62; // -
    // int LSTORE_0 = 63; // -
    // int LSTORE_1 = 64; // -
    // int LSTORE_2 = 65; // -
    // int LSTORE_3 = 66; // -
    // int FSTORE_0 = 67; // -
    // int FSTORE_1 = 68; // -
    // int FSTORE_2 = 69; // -
    // int FSTORE_3 = 70; // -
    // int DSTORE_0 = 71; // -
    // int DSTORE_1 = 72; // -
    // int DSTORE_2 = 73; // -
    // int DSTORE_3 = 74; // -
    // int ASTORE_0 = 75; // -
    // int ASTORE_1 = 76; // -
    // int ASTORE_2 = 77; // -
    // int ASTORE_3 = 78; // -
    int IASTORE = 79; // visitInsn
    int LASTORE = 80; // -
    int FASTORE = 81; // -
    int DASTORE = 82; // -
    int AASTORE = 83; // -
    int BASTORE = 84; // -
    int CASTORE = 85; // -
    int SASTORE = 86; // -
    int POP = 87; // -
    int POP2 = 88; // -
    int DUP = 89; // -
    int DUP_X1 = 90; // -
    int DUP_X2 = 91; // -
    int DUP2 = 92; // -
    int DUP2_X1 = 93; // -
    int DUP2_X2 = 94; // -
    int SWAP = 95; // -
    int IADD = 96; // -
    int LADD = 97; // -
    int FADD = 98; // -
    int DADD = 99; // -
    int ISUB = 100; // -
    int LSUB = 101; // -
    int FSUB = 102; // -
    int DSUB = 103; // -
    int IMUL = 104; // -
    int LMUL = 105; // -
    int FMUL = 106; // -
    int DMUL = 107; // -
    int IDIV = 108; // -
    int LDIV = 109; // -
    int FDIV = 110; // -
    int DDIV = 111; // -
    int IREM = 112; // -
    int LREM = 113; // -
    int FREM = 114; // -
    int DREM = 115; // -
    int INEG = 116; // -
    int LNEG = 117; // -
    int FNEG = 118; // -
    int DNEG = 119; // -
    int ISHL = 120; // -
    int LSHL = 121; // -
    int ISHR = 122; // -
    int LSHR = 123; // -
    int IUSHR = 124; // -
    int LUSHR = 125; // -
    int IAND = 126; // -
    int LAND = 127; // -
    int IOR = 128; // -
    int LOR = 129; // -
    int IXOR = 130; // -
    int LXOR = 131; // -
    int IINC = 132; // visitIincInsn
    int I2L = 133; // visitInsn
    int I2F = 134; // -
    int I2D = 135; // -
    int L2I = 136; // -
    int L2F = 137; // -
    int L2D = 138; // -
    int F2I = 139; // -
    int F2L = 140; // -
    int F2D = 141; // -
    int D2I = 142; // -
    int D2L = 143; // -
    int D2F = 144; // -
    int I2B = 145; // -
    int I2C = 146; // -
    int I2S = 147; // -
    int LCMP = 148; // -
    int FCMPL = 149; // -
    int FCMPG = 150; // -
    int DCMPL = 151; // -
    int DCMPG = 152; // -
    int IFEQ = 153; // visitJumpInsn
    int IFNE = 154; // -
    int IFLT = 155; // -
    int IFGE = 156; // -
    int IFGT = 157; // -
    int IFLE = 158; // -
    int IF_ICMPEQ = 159; // -
    int IF_ICMPNE = 160; // -
    int IF_ICMPLT = 161; // -
    int IF_ICMPGE = 162; // -
    int IF_ICMPGT = 163; // -
    int IF_ICMPLE = 164; // -
    int IF_ACMPEQ = 165; // -
    int IF_ACMPNE = 166; // -
    int GOTO = 167; // -
    int JSR = 168; // -
    int RET = 169; // visitVarInsn
    int TABLESWITCH = 170; // visiTableSwitchInsn
    int LOOKUPSWITCH = 171; // visitLookupSwitch
    int IRETURN = 172; // visitInsn
    int LRETURN = 173; // -
    int FRETURN = 174; // -
    int DRETURN = 175; // -
    int ARETURN = 176; // -
    int RETURN = 177; // -
    int GETSTATIC = 178; // visitFieldInsn
    int PUTSTATIC = 179; // -
    int GETFIELD = 180; // -
    int PUTFIELD = 181; // -
    int INVOKEVIRTUAL = 182; // visitMethodInsn
    int INVOKESPECIAL = 183; // -
    int INVOKESTATIC = 184; // -
    int INVOKEINTERFACE = 185; // -
    int INVOKEDYNAMIC = 186; // visitInvokeDynamicInsn
    int NEW = 187; // visitTypeInsn
    int NEWARRAY = 188; // visitIntInsn
    int ANEWARRAY = 189; // visitTypeInsn
    int ARRAYLENGTH = 190; // visitInsn
    int ATHROW = 191; // -
    int CHECKCAST = 192; // visitTypeInsn
    int INSTANCEOF = 193; // -
    int MONITORENTER = 194; // visitInsn
    int MONITOREXIT = 195; // -
    // int WIDE = 196; // NOT VISITED
    int MULTIANEWARRAY = 197; // visitMultiANewArrayInsn
    int IFNULL = 198; // visitJumpInsn
    int IFNONNULL = 199; // -
    // int GOTO_W = 200; // -
    // int JSR_W = 201; // -

```
无论是Java编写的各种框架/中间件，还是我们平常编写的业务代码，其方法体里都是由这些200个操作码和操作数构成，它们是字节码里重要的组成部分。正是因为字节码与具体的操作系统、处理器架构无关，才有了JVM语言的跨平台性，而字节码的"强规范性"也孕育出了很多字节码相关技术。

"class文件"按照一定规范去描述一个类，如类名、父类、接口、常量池、属性、方法、注解等信息，在JVM看来，字节码无异于一个byte数组，如果能够在程序运行时按照规范去创建/修改这个byte数组，那我们就可以做到动态地控制程序的各种行为。然而去编写或修改一个符合规范的byte数组是很繁琐的事情，而bytecode manipulation则是辅助我们去构建这个"byte数组"的技术。没有找到好听的翻译，就称bytecode manipulation为动态字节码技术吧。

我们在日常开发中，可能很少直接接触到动态字节码技术，但我们用到的很多框架或工具内部其实都运用了这些技术，例如Spring Framework里使用cglib生成动态代理，来实现事务、异步方法、缓存、AOP等。另外还有MyBatis使用JDK动态代理、JRebel使用javassist和asm、Mockito/Hibernate使用Byte-Buddy等。当然还包括各种分析、Debug、Profiler等工具，其实动态字节码技术早已遍布在依托JVM的各个领域。


## 为什么需要动态字节码技术

先给出一个等于没给的结论: 因为它能增加Java这门静态类型语言的动态能力，完成很多原本只有动态类型语言能完成的事。

比如让Java动态地去创建类、修改和重新加载已经被加载的类等，当然你能够操控字节码其实就约等于你能干一切事情，全靠你的想象力。

我们先谈谈Java语言本身。Java是一门静态类型语言，静态类型语言要求
变量的类型需要显式指定且在编译期间是可知的，即当声明了变量后，其类型是无法修改的。
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

但很多时候，尤其是在实现通用框架的时候，"框架"是无法知道"用户"使用哪些类型的，但框架往往也需要去调用用户定义的代码，那问题来了，框架怎么在不知道用户类型的情况下去调用用户方法呢？对于Python这种动态类型语言，由于在编码期间IDE无法推断变量类型，对象的方法调用是很"随意"的, 如以下片段：
```python
# 由框架提前"编译"
class DriverProxy:
   def __init__(self, driver):
      self.driver = driver
   def execute_sql(sql):
      self.driver.execute_sql(sql) # 不会报错
```
框架完全可以在不知道用户类型的情况下去"编译"以上代码，用户在使用框架的时候自行传入实现了execute_sql方法的实例即可。

对于Java这门静态类型语言来说，在编码时如果不确定变量的具体类型，是无法调用相应的方法的。那Java的一些框架如何去调用用户代码呢？我知道的有以下两种方式：
（便于说明，这里仅仅用一个静态代理来代表"第三方框架"）
`方式1--多态 （接口和非final类）`
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
Java是一门面向对象的语言，它具有多态性。如上，"框架代码"不用知道用户的具体类型就能被编译。用户在使用框架的时候传入一个具体的实现类，借助虚函数表等技术就能在运行的时候去动态绑定到具体的"用户方法"。

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

当然以上问题不能遮盖反射在各种场景发挥的巨大作用，动态字节码技术往往也会搭配反射API使用。

反射为Java提供了"动态地读取类"的能力，而动态字节码技术却更侧重"动态地创建类和修改类"的能力。如果上面的这个"Secure框架"用动态字节码技术实现就会很容易和直观，一般思路为继承当前类，重载所有父类里被@Secure注解的方法，方法体里首先执行统一的安全校验逻辑，校验通过后再调用父类的"原始方法"，实际这也是动态字节码技术用的最多的一个场景---动态代理。



## 常用动态字节码技术介绍
接下来进入正题，介绍几个常用的动态字节码技术，分别为JDK动态代理、javassist、asm、cglib、byte-buddy。

目前动态字节码技术最常用的场景还是动态代理，动态代理可以增强用户定义的方法，比如增加日志、缓存逻辑、事务逻辑等。
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

javassist是一个使用起来相对比较简单的技术，它的API比较贴近Java的反射API。

它使用自己实现的一个Java编译器（不支持泛型），在调用一些创建/修改方法的API时，需要将java源码作为字符串入参，在编写一些复杂定制逻辑时还是很容易出错。
建议作为学习使用。

`动态代理`
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

`去除所有println`
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

`创建一个class`
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
asm是一个专注于字节码领域的框架，很多优秀的框架都使用了它，例如cglib、byte-buddy实质就是对asm的封装，就连JDK本身也将asm打包到了自己的命名空间里。

![](https://bj.bcebos.com/cookie/img_5.png))

从上图也可以看出很多框架都将ASM打包到了自己的命名空间里，所以在使用ASM的时候一定要注意不要import错包了。

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

从这个简单的POJO类可以看出，使用ASM visitor api就像在手动书写".class"字节码文件一样：你需要按照一定顺序去调用各个API，如果顺序出现问题则会"编写"出不合法的class文件，所以这对不了解class文件以及JVM底层执行方法逻辑的
小伙伴不是很友好。asm提供出来的api实在是太琐碎了，但基于观察者这种设计模式，对于一些简单的场景，我们只需要重载相应的方法即可。

`使用模板`
```java
public class Main {
    public static void main(String[] args) throws Exception {
        ClassReader classReader = new ClassReader("..."); // class全限定名
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES); // 自动计算操作数栈最大深度和局部变量表的变量个数以及stack map frames
        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9, classWriter) {
            // override 各种方法
        };
        classReader.accept(classVisitor, 0);
        byte[] classBytes = classWriter.toByteArray(); // 得到"字节码流"

        // ... 后续操作
    }
}
```


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

`修改实例属性的access flag`
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
                return super.visitField(Opcodes.ACC_PUBLIC, name, descriptor, signature, value); // 这里其实需要进行一些位运算，否则会丢失其它flag，如volatile、static
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

`打印日志`
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

接下来我们介绍一下byte-buddy这个框架，它实质也是对ASM框架的封装。相较于前面几个前辈，byte-buddy出身较晚，它2013年底才开源，且从commit提交记录和社区问答里发现这个项目至今几乎一直是[Rafael Winterhalter](https://github.com/raphw) 一个人在维护。但是它的api使用体验是比较好的，最重要的是它定义了各种选择器(ElementMatcher)，让你可以非常方便地选择对哪些类的哪些方法进行修改，这些选择器让用户自行用ASM API实现是非常繁琐的。并且它也暴露出了部分ASM visitor api，这使得byte-buddy兼具了易用性和功能完备性。

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

第一次接触Byte-Buddy的小伙伴可能会觉得这个api使用起来比较麻烦，但实际上这是为了能让用户能做更细腻度的控制。首先subclass代表你要继承哪个类或者实现哪个接口，之后method(...)用于筛选你想要进行拦截/重载的方法，以上就是过滤掉了Object声明的一些方法，紧接着intercept(...)就是方法的实现，这里最常用的api就是MethodDelegation(方法委托),它将原先的方法重载并委托给了其它类/对象。

我们重点看看这个LogImplementation这个类的实现，一般被委托的类会实现一个且仅一个方法，而最重要的地方就是方法、方法参数上的注解。@RuntimeType加在了方法上，代表这个方法的返回值可以是任意类型，假如不用这个注解，那么返回值必须和被委托方法(即sayHello(String))保持一致：void。而方法参数的注解@Origin, 指明当前method是被委托方法，而@SuperCall，指明当前callable包含了被委托方法的执行逻辑，我们只需要执行callable.call()就能触发被委托方法的逻辑。除了这些注解还有几个常用的注解, 例如`@This Object this`指明当前object是代理类实例的this指针，`@AllArguments Object[] args`指明args是方法的所有调用参数。一旦参数上加上了这些注解，用byte-buddy操作字节码生成的类在调用被委托方法前，会将这些被注解的参数按照语义赋值后，再调用LogImplementation.invoke方法,还有其它注解这里就不一一举例了。

## java agent

### java agent是什么

java agent是java很早就有的一个概念，使用动态字节码技术时一般都会和java agent搭配使用。那什么是java agent呢？

我的理解是它是一个类加载的网关：当类被加载到类加载器之前，java agent可以对已经读取的字节码流进行修改，从而来控制类的各种行为。既然它是一个网关，那我们就可以对每一个加载到JVM里的类（当然，部分jdk的类会在使用java agent前初始化）进行统一控制，而不用像上面所有例子一样针对每一个类都要手动去编写一大段代码,这里我们先看一下java agent如何使用, 它有两种使用方式，一种是是可插拔式的静态使用方式(通过启动时指定jvm参数)，一种是利用java的attach api的动态使用方式（动态attach到已经启动的JVM进程）

### 静态使用方式

`创建一个类，像main方法一样写一个固定签名的premain方法`

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

打包成jar包(一般为fat-jar)，记住jar包的路径，在你启动JVM进程前增加JMV参数(替换成自己的路径)

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

以上和我们用byte-buddy实现动态代理很像，但值得注意的是通过java agent增强的类在classloader看来还是原先的类，并非代理类。

### 动态使用方式
首先介绍Attach API
#### Java Attach API
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

      VirtualMachine virtualMachine = null;
      try {
         // 模拟jstack
         virtualMachine = VirtualMachine.attach("17729"); // 替换成你自己的pid
         HotSpotVirtualMachine hotSpotVirtualMachine = (HotSpotVirtualMachine) virtualMachine;
         InputStream inputStream = hotSpotVirtualMachine.remoteDataDump();
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
         bufferedReader.lines().forEach(System.out::println);

         // 模拟jmap -histo:live
         inputStream = hotSpotVirtualMachine.heapHisto();
         bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
         bufferedReader.lines().forEach(System.out::println);

         // 动态加载java agent, 无返回值
         hotSpotVirtualMachine.loadAgent("{YourPath}/{your-agent}.jar");
      } finally {
         if (Objects.nonNull(virtualMachine)) {
            virtualMachine.detach();
         }
      }
   }
}
```
简单来说, attach api可以与一个正在运行的JVM进程进行通信(会起一个Attach Listener线程)，而loadAgent方法则用来动态加载java agent。
一旦调用了这个api, 被attach的jvm进程会在Attach Listener线程里去执行对应agent jar入口类的agentmain方法，它与premain一样，需要在MANIFEST.MF里增加配置。
```
Agent-Class: com.xxywebsite.bytecode.common.JavaAgentTemplate
```

并和main方法、premain方法一样，需要定义一个固定入参、返回值的agentmain方法：
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

打包成jar包(一般为fat-jar)，记住jar包的路径，通过Java Attach Api让目标JVM动态加载agent
```java
public class LoadAgentApp {
   public static void main(String[] args) throws Exception {
      VirtualMachine virtualMachine = null;
      try {
         virtualMachine = VirtualMachine.attach("17729"); // 替换成你自己的pid
         HotSpotVirtualMachine hotSpotVirtualMachine = (HotSpotVirtualMachine) virtualMachine;
         // 动态加载java agent
         hotSpotVirtualMachine.loadAgent("{YourPath}/{your-agent}.jar");
      } finally {
         if (Objects.nonNull(virtualMachine)) {
            virtualMachine.detach();
         }
      }
   }
}
```

目前进程会使用Attach Listener线程执行agentmain方法，之后"新类"在被加载到类加载器前都会被ClassFileTransformer拦截。

#### Self Attaching
从上面的例子可以看出动态使用Java Agent的步骤也挺繁琐的，但实际上在开发调试过程中我们可以使用self-attach来免除上面的步骤。ByteBuddy已经为我们封装好self-attaching api，我们只需要调用ByteBuddyAgent.install(), 就能返回关键的instrumentation对象。
```java
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Phaser;

public class DynamicJavaAgentTest {
   public static void main(String[] args) {
       
      // self-attaching 
      // 1、获取当前JVM进程的pid
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

#### 重新加载类
很多时候目标JVM已经运行很久了，该加载的类都已经加载得差不多了，这个时候如果我们需要增强一些方法，就需要重新加载已经加载的类，我们先看个例子
```java
public class ReloadStringClassTest {
    public static void main(String[] args) throws Exception {
        // self-attach
        Instrumentation instrumentation = ByteBuddyAgent.install();
        
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if (className.replace("/", ".").equals("java.lang.String")) {
                    System.out.println("java.lang.String被重新加载");
                }
                return classfileBuffer;
            }
        }, true);
        instrumentation.retransformClasses(String.class); // 打印java.lang.String被重新加载
        instrumentation.retransformClasses(String.class); // 打印java.lang.String被重新加载（可被重新加载多次）
    }
}
```
使用ByteBuddyAgent API可以让我们灵活选择要重新加载哪些类和增强哪些方法。
比如我们想重新加载Object，"破坏"toString方法
```java
public class ModifyObjectToStringTest {
    public static class A {
        @Override
        public String toString() {
            return super.toString();
        }
    }

    // 修改Object toString方法，并重新加载Object类
    public static void main(String[] args) {
        Object o = new Object();
        System.out.println(o.toString());

        // self attaching
        Instrumentation instrumentation = ByteBuddyAgent.install();

        new AgentBuilder
                .Default()
                .ignore(nameStartsWith("zxczczx.zxczc.xzczxc")) // 这一行的意义？ TODO
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(named("java.lang.Object"))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                        return builder
                                .method(named("toString"))
                                .intercept(FixedValue.value("123"));
                    }
                })
                .installOn(instrumentation);


        System.out.println(o.toString()); // 123
    }
}
```

一般重新加载类会和Advice API使用，Advice相较于Byte Buddy里常用的MethodDelegation还是有很大的不同，它们都用于增强方法，前者是直接在原方法的前后增加逻辑，后者是通过委托给其它方法执行。



#### 其它用法
Java Attach API只暴露了有限的方法，而loadAgent可以让我们执行指定jar包的agentmain方法，除了增加ClassFileTransformer外，实际上你可以在agentmain
执行任意逻辑，例如像[arthas](https://github.com/alibaba/arthas)一样，启动很多监听线程，后续可以直接与外界进行进程通信。
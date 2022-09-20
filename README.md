# Java Bytecode Manipulation Exploration

## 什么是Bytecode Manipulation？
字节码是JVM平台语言（如Java、Kotlin、Scala、groovy）的概念，本文以Java为例。Java源文件在被
编译的时候，并不会直接被编译成机器代码，而会被编译成以.class后缀的
字节码文件并一般存储到硬盘上，只有程序真正执行的时候，".class"文件才会被翻译成与操作系统、处理器架构
适配的可执行机器代码，而".class"文件存储的内容其实就是字节码。
.class文件是一种"二进制文件"，它按照顺序记录了字节码版本、类的各种元信息，如
类名、父类、接口、实例属性、实例方法、静态属性、静态方法、注解等，本质上它就是一个byte数组，如果我们能按照字节码规范去创建/修改一个已存在的byte数组，那我们就可以做到创建类以及修改类的各种行为。
然而去造一个byte数组是一件很困难的事，而bytecode manipulation则是辅助我们去构建这个"byte数组"的技术。


#### 字节码的"各种形式"
// TODO 截图...


#### 字节码在各种框架的应用
// TODO 举例...


## 为什么需要Bytecode Manipulation?
先给出结论: TODO

在讨论为什么需要字节码增强技术之前，我们先谈谈Java语言本身。Java是一门静态类型语言，静态类型语言要求
变量的类型需要显式指定且在编译期间是可知的，即当声明了变量后，其类型是无法修改的。
```java
public class Main {
    public void main(String[] args) {
        int x = 5;
        double y = 6.0;
        x = y  // 编译报错
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
但很多时候，尤其是在实现通用框架的时候，"框架"是无法知道"用户"使用哪些类型的，但框架往往也需要去调用用户定义的代码，那问题来了，框架怎么
在不知道用户类型的情况下去调用用户方法呢？对于Python这种动态类型语言，由于在编码期间IDE无法推断变量类型，这从另外一个角度上来说"框架"可以
定义未知类型变量的任意方法，如以下代码是不会报错的, 用户只需要在使用的时候传入实现了execute_sql的方法就行。
```python
class DriverProxy:
   def __init__(self, driver):
      self.driver = driver
   def execute_sql(sql):
      self.driver.execute_sql(sql) # 不会报错
    
 
```
对于Java这门静态类型语言来说，如果不确定变量的具体类型，是无法调用相应的方法的。那在Java里，通用框架一般如何去调用用户代码呢？介绍两种方式
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
方式1借助接口，框架不用关心用户传入的变量具体是什么类型，只要用户传入任意一个具体的实现类，框架在运行时就能调用到具体用户代码啦。
当然了这也得益于面向对象语言多态特性。

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
如上例，SecureMethodUtil用于执行用户代码前，校验当前用户是否有执行权限。"框架"不用在编译
时关心用户类型是什么，借助反射特性，框架也能在运行时执行用户代码。
事实上反射是Java语言很强大的特性，它提供了运行时获取类的各种元数据的能力（得益于字节码），
在框架里经常被使用。拿fastJson、easyExcel举例，它们就是动态去获取当前对象类的所有实例属性的getter/setter方法，
这样在类去拓展/修改字段时，相应用户代码不用做任何调整就能实现json字符串、excel文件的变化。
反射为Java这门静态类型语言提供了一定的动态特性，但也存在一些问题。
1. 第一个是性能问题。通过class对象去获取方法(Method)和属性(Field)的开销是很大的，但一旦缓存了Method、Field对象，之后对方法/属性的访问会快很多。
尽管在执行方式时会调用native方法(sun.reflect.NativeMethodAccessorImpl#invoke0), 部分native方法的调用会比Java方法慢，而JVM也对此做了优化，所以在很多场景中，反射不会出现性能瓶颈，感兴趣的可以看看[java relfection inflation]()
   
2. 大量使用反射，在项目工程里无法很快发现一些属性和方法的依赖关系。如果属性和方法是显式调用的，那么很容易借助IDE知道这些属性或方法被哪些地方依赖了，但一旦用了反射，IDE就束手无策了。
3. 第二个也是我认为最主要的问题，反射抛弃了Java静态语言类型的优势，即编译时类型检查。借助反射去执行一个方法时，method.invoke(...)的传参是否正确只有在运行时才能发现，"框架"显然不希望暴露给"用户"反射API, 因为这会增加很多不确定性。

当然以上问题不能遮盖反射在各种场景发挥的巨大作用。

`接下来进入正题---Bytecode Manipulation`

Bytecode Manipulation让用户能够去操作字节码，也就能够控制类的各种行为，具体能做什么取决于使用者的想象力和创造力。


## 常用Bytecode Manipulation框架简单介绍
### jdk proxy
  jdk proxy即我们常说的JDK动态代理，实际上它是jdk内置的工具类，借助它我们能快速地实现动态代理。
```java

```

### javassist

### asm

### cglib

### byte-buddy


### 用哪一个框架？
  


## java agent机制 







## 
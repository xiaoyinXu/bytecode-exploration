package com.xxywebsite.bytecode.cglib;

import com.xxywebsite.bytecode.common.HelloService;
import com.xxywebsite.bytecode.common.HelloServiceImpl;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
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

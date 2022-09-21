package com.xxywebsite.bytecode.common;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public class ByteArrayClassLoader extends ClassLoader {
    public Class<?> defineClazz(byte[] classBytes) {
        return defineClass(classBytes, 0, classBytes.length);
    }
}

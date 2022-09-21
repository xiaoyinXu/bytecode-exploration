package com.xxywebsite.bytecode.common;

/**
 * @author xuxiaoyin
 * @since 2022/9/21
 **/
public interface DynamicProxyFactory {
    <T> T createProxy();
}

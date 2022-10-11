package com.xxywebsite.attach.tool.io;

/**
 * @author xuxiaoyin
 * @since 2022/10/11
 **/
public class ClientMain {
    public static void main(String[] args) {
        DefaultMessageClient defaultMessageClient = new DefaultMessageClient(7779);
        defaultMessageClient.start();
    }
}

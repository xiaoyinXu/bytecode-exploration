package com.xxywebsite.attach.tool.io;

/**
 * @author xuxiaoyin
 * @since 2022/10/11
 **/
public class ServerMain {
    public static void main(String[] args) {
        DefaultMessageServer defaultMessageServer = new DefaultMessageServer(7779);
        defaultMessageServer.start();
    }
}

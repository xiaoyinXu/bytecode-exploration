package com.xxywebsite.attach.tool.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * @author xuxiaoyin
 * @since 2022/10/11
 **/
public class DefaultMessageClient implements AbstractMessageClient {
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;

    public DefaultMessageClient(int port) {
        this.port = port;
    }


    @Override
    public void start() {
        try {
            selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open();


            boolean connected = socketChannel.connect(new InetSocketAddress("localhost", port));
            if (!connected) {
                throw new RuntimeException("连接失败");
            }

            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);

            new Thread(() -> {
                BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
                String msg;
                try {
                    while ((msg = bf.readLine()) != null) {

                        int index = 0;
                        int bufferSize = 24;
                        while (index < msg.length()) {

                            int remainLength = msg.length() - index;
                            int readLength = Math.min(remainLength, bufferSize);
                            byte[] tempBytes = msg.substring(index, index + readLength).getBytes(StandardCharsets.UTF_8);
                            ByteBuffer writeByteBuffer = ByteBuffer.allocate(tempBytes.length);

                            writeByteBuffer.put(tempBytes);
                            writeByteBuffer.flip();

                            socketChannel.write(writeByteBuffer);

                            index += readLength;
                        }

                        if (msg.toLowerCase().equals("exit")) {
                            stop();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, "write-thread").start();

            while (selector.select() > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();

                    if (selectionKey.isReadable()) {
                        StringBuilder sb = new StringBuilder();
                        int readBytes = 0;
                        do {
                            ByteBuffer byteBuffer = ByteBuffer.allocate(24);
                            try {
                                readBytes = socketChannel.read(byteBuffer);
                                byteBuffer.flip();
                                byte[] bytes = new byte[byteBuffer.remaining()];
                                byteBuffer.get(bytes);
                                sb.append(new String(bytes, "utf-8"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } while (readBytes > 0);


                        String responseMsg = sb.toString();
                        if (responseMsg != null && !responseMsg.isEmpty()) {
                            System.out.println(String.format("收到服务端反馈消息:%s", responseMsg));
                        }
                    } else {
                        throw new RuntimeException("unknown selectionKey type");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}

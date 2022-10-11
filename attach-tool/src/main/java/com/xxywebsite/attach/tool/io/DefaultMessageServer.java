package com.xxywebsite.attach.tool.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * @author xuxiaoyin
 * @since 2022/10/11
 **/
public class DefaultMessageServer implements AbstractMessageServer {
    private ServerSocketChannel serverSocketChannel;

    private Selector selector;

    private int port;

    public DefaultMessageServer(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress("localhost", port));

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


            startWriteThread();

            while (selector.select() > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();

                    if (selectionKey.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (selectionKey.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                        StringBuilder sb = new StringBuilder();
                        int readBytes;
                        do {
                            ByteBuffer readByteBuffer = ByteBuffer.allocate(24);
                            try {
                                readBytes = socketChannel.read(readByteBuffer);
                            } catch (IOException e) {
                                readBytes = -1;
                                break;
                            }

                            readByteBuffer.flip();
                            byte[] bytes = new byte[readByteBuffer.remaining()];
                            readByteBuffer.get(bytes);
                            sb.append(new String(bytes, "utf-8"));
                        } while (readBytes > 0);

                        if (readBytes == -1) {
                            System.out.println("客户端关闭");
                            socketChannel.close();
                        }

                        String msg = sb.toString();
                        System.out.println(String.format("收到客户端消息: %s", msg));

                        // 反馈消息
                        String responseMsg = "";
                        switch (msg.toLowerCase()) {
                            case "hello": {
                                responseMsg = "Hi";
                                break;
                            }
                            case "exit": {
                                stop();
                                break;
                            }
                            default: {
                                responseMsg = "Unknown Command";
                            }
                        }

                        int index = 0;
                        int bufferSize = 24;
                        while (index < responseMsg.length()) {

                            int remainLength = responseMsg.length() - index;
                            int readLength = Math.min(remainLength, bufferSize);
                            byte[] tempBytes = responseMsg.substring(index, index + readLength).getBytes(StandardCharsets.UTF_8);
                            ByteBuffer writeByteBuffer = ByteBuffer.allocate(tempBytes.length);

                            writeByteBuffer.put(tempBytes);
                            writeByteBuffer.flip();

                            socketChannel.write(writeByteBuffer);

                            index += readLength;
                        }

                    } else {
                        throw new RuntimeException("unknown selectionKey type");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startWriteThread() {
        new Thread(() -> {
            BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
            String msg;
            try {
                while ((msg = bf.readLine()) != null) {
                    msg = String.format("服务端发送消息:%s", msg);

                    Set<SelectionKey> selectionKeys = selector.keys();
                    for (SelectionKey selectionKey : selectionKeys) {
                        SelectableChannel channel = selectionKey.channel();
                        if (channel instanceof SocketChannel) {
                            SocketChannel socketChannel = (SocketChannel) channel;

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
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "write-thread").start();
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        ByteBuffer readByteBuffer = ByteBuffer.allocateDirect(5);
        readByteBuffer.put("123456".getBytes(StandardCharsets.UTF_8));
    }
}

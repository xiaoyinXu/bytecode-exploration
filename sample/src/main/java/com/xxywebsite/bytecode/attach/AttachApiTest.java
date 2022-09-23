package com.xxywebsite.bytecode.attach;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import sun.tools.attach.HotSpotVirtualMachine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author xuxiaoyin
 * @since 2022/9/23
 **/
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

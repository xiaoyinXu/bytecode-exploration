package com.xxywebsite.bytecode.attach;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import sun.tools.attach.HotSpotVirtualMachine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

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

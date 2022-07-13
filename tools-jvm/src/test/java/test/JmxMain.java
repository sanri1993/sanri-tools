package test;

import java.io.*;
import java.lang.management.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.google.common.base.Charsets;
import com.sanri.tools.modules.jvm.service.dtos.HeapHistogramImpl;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.junit.Test;

import com.sun.management.*;
import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.OperatingSystemMXBean;
import com.sun.management.ThreadMXBean;

public class JmxMain {


    @Test
    public void testJmx() throws IOException {
        JMXServiceURL jmxServiceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://10.101.72.42:59949/jmxrmi");
        JMXConnector connect = JMXConnectorFactory.connect(jmxServiceURL, null);

        MBeanServerConnection mBeanServerConnection = connect.getMBeanServerConnection();
        String[] domains = mBeanServerConnection.getDomains();
        for (String domain : domains) {
            System.out.println(domain);
        }

        connect.close();
    }

    @Test
    public void test0(){
        System.out.println(JmxMain.class.getProtectionDomain().getCodeSource().getLocation());
    }

    /**
     * 获取操作系统信息, 可以获取的信息包含
     * 总物理内存,提交的虚拟内存,空闲物理内存,空闲交换区大小,总交换区大小
     * 进程CPU 负载,进程CPU 时间,系统 CPU 负载, 系统平均负载
     * 系统架构,核心数, 系统名称,版本
     * @throws IOException
     */
    @Test
    public void test1() throws IOException {
        MBeanServerConnection mbeanConnection = getmBeanServerConnection();

//        final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.newPlatformMXBeanProxy(mbeanConnection,
//                ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
//        final double processCpuLoad = operatingSystemMXBean.getProcessCpuLoad();
//        System.out.println(processCpuLoad);

        final OperatingSystemMXBean operatingSystemMXBean = JMX.newMXBeanProxy(mbeanConnection, createBeanName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME), OperatingSystemMXBean.class);
        System.out.println(operatingSystemMXBean.getFreeSwapSpaceSize());
    }

    /**
     * 获取线程信息
     * 线程 CPU 时间,用户时间,分配的字节数
     * 线程数量,当前线程CPU 时间,当前线程用户时间
     * 线程信息 , 状态, 线程名,锁等待, 堆栈
     * @throws IOException
     */
    @Test
    public void test2() throws IOException {
        final ThreadMXBean threadMXBean = JMX.newMXBeanProxy(getmBeanServerConnection(), createBeanName(ManagementFactory.THREAD_MXBEAN_NAME),
                ThreadMXBean.class);
        final long[] allThreadIds = threadMXBean.getAllThreadIds();
        final long totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();
        for (long allThreadId : allThreadIds) {
            final ThreadInfo threadInfo = threadMXBean.getThreadInfo(allThreadId);
            System.out.println(threadInfo);
        }
    }

    /**
     * 类加载器
     * 总共加载的类数量, 加载的类数量, 卸载的类数量
     * @throws IOException
     */
    @Test
    public void test3() throws IOException {
        final ClassLoadingMXBean classLoadingMXBean = ManagementFactory.newPlatformMXBeanProxy(getmBeanServerConnection(),
                ManagementFactory.CLASS_LOADING_MXBEAN_NAME, ClassLoadingMXBean.class);
        final int loadedClassCount = classLoadingMXBean.getLoadedClassCount();
    }

    /**
     * 元数据区,老年代,存活区, 新生代, code cache , 压缩类空间
     * mapper 内存, 直接内存
     * @throws IOException
     */
    @Test
    public void test4() throws IOException {
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getPlatformMXBeans(getmBeanServerConnection(),
                MemoryPoolMXBean.class);
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            System.out.println(memoryPoolMXBean);
        }

        final List<BufferPoolMXBean> bufferPoolMXBeans = ManagementFactory.getPlatformMXBeans(getmBeanServerConnection(),
                BufferPoolMXBean.class);
        for (BufferPoolMXBean bufferPoolMXBean : bufferPoolMXBeans) {
            System.out.println();
        }

    }

    /**
     * 垃圾收集器信息
     * 回收次数, 回收占用时间, 上次垃圾回收信息
     * @throws IOException
     */
    @Test
    public void test5() throws IOException {
        List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getPlatformMXBeans(getmBeanServerConnection(),GarbageCollectorMXBean.class);
        for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
            final GcInfo lastGcInfo = gcMXBean.getLastGcInfo();
            System.out.println(lastGcInfo);
        }
    }

    /**
     * 获取运行时信息
     * 系统属性列表
     * 启动时间
     * classpath
     */
    @Test
    public void test6() throws IOException {
        final RuntimeMXBean runtimeMXBean = ManagementFactory.newPlatformMXBeanProxy(getmBeanServerConnection(), ManagementFactory.RUNTIME_MXBEAN_NAME,
                RuntimeMXBean.class);
        final Map<String, String> systemProperties = runtimeMXBean.getSystemProperties();
        final Iterator<Map.Entry<String, String>> iterator = systemProperties.entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry<String, String> next = iterator.next();
            System.out.println(next.getKey()+"="+next.getValue());
        }
    }

    /**
     * 获取 jvm 参数信息
     * 可以 dump 堆
     */
    @Test
    public void test7() throws IOException {
        final HotSpotDiagnosticMXBean hotSpotDiagnosticMXBean = ManagementFactory.newPlatformMXBeanProxy(getmBeanServerConnection(),
                "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
        final List<VMOption> diagnosticOptions = hotSpotDiagnosticMXBean.getDiagnosticOptions();
        for (VMOption diagnosticOption : diagnosticOptions) {
            final VMOption.Origin origin = diagnosticOption.getOrigin();
            System.out.println(diagnosticOption.getName()+"="+diagnosticOption.getValue()+" -> "+origin);
        }

        // 第一个参数是文件路径 , 是二个参数是是否 live
        hotSpotDiagnosticMXBean.dumpHeap("d:/test/a.bin",true);

    }

    private static final String DIAGNOSTIC_COMMAND_MXBEAN_NAME = "com.sun.management:type=DiagnosticCommand";

    /**
     * 获取 内存直方图
     */
    @Test
    public void test8() throws IOException, MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException {
        final MBeanServerConnection mBeanServerConnection = getmBeanServerConnection();
        ObjectName diagCommName = new ObjectName(DIAGNOSTIC_COMMAND_MXBEAN_NAME);
        // 执行 gc
//        final Object gcRun = mBeanServerConnection.invoke(diagCommName, "gcRun", null, null);
//        System.out.println(gcRun);

        // 获取类直方图
        String[] signature = new String[] {String[].class.getName()};
        Object[] params = new Object[]{new String[]{"-all="}};
//        final String histogramText = (String) mBeanServerConnection.invoke(diagCommName, "gcClassHistogram", params, signature);
//
//        final HeapHistogramImpl heapHistogram = new HeapHistogramImpl(histogramText);
//        System.out.println(heapHistogram);

        // 获取 vmFlags
//        params = new Object[]{new String[0]};
        final String vmFlags = (String) mBeanServerConnection.invoke(diagCommName, "vmFlags", params, signature);
//        System.out.println(vmFlags);



    }

    Pattern pattern = Pattern.compile(":?=\\s(.+)");

    @Test
    public void test81() throws IOException {
        final InputStream resourceAsStream = JmxMain.class.getClassLoader().getResourceAsStream("testvmflags.txt");
        final String toString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
        Scanner scanner = new Scanner(toString);
        scanner.nextLine();
        while (scanner.hasNext()) {
            final String type = scanner.next();
            final String name = scanner.next();
            switch (type) {
                case "bool":
                    break;
                case "uintx":
                case "intx":
//                System.out.println(scanner.nextInt());
                    break;
                case "double":
                    break;
                case "ccstrlist":
                    break;
                case "ccstr":
                    break;

            }
        }
        resourceAsStream.close();

    }

    /**
     * 获取 spring beans
     */
    @Test
    public void test9() throws IOException, MBeanException, InstanceNotFoundException, ReflectionException {
        final MBeanServerConnection mBeanServerConnection = getmBeanServerConnection();
        final ObjectName beanName = createBeanName("org.springframework.boot:type=Endpoint,name=Beans");
        final Map beans = (Map) mBeanServerConnection.invoke(beanName, "beans", null, null);
        System.out.println(beans);
    }

    @Test
    public void test10() throws IOException, MBeanException, InstanceNotFoundException, ReflectionException {
        final MBeanServerConnection mBeanServerConnection = getmBeanServerConnection();
        final ObjectName beanName = createBeanName("org.springframework.boot:type=Endpoint,name=Loggers");

        final Map loggers = (Map) mBeanServerConnection.invoke(beanName, "loggers", null, null);
        System.out.println(loggers);
    }

    @Test
    public void test11() throws IOException {
        final MBeanServerConnection mBeanServerConnection = getmBeanServerConnection();
        final String[] domains = mBeanServerConnection.getDomains();
//        final Set<ObjectName> objectNames = mBeanServerConnection.queryNames(createBeanName("java.lang:type=*"), null);
        final Set<ObjectName> objectNames = mBeanServerConnection.queryNames(null, null);

        System.out.println(StringUtils.join(domains,"\n"));

    }

    @Test
    public void test12() throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException {
        final ObjectName beanName = createBeanName("com.sun.management:type=DiagnosticCommand");
        final String keyPropertyListString = beanName.getKeyPropertyListString();
        System.out.println(keyPropertyListString);

        final MBeanServerConnection mBeanServerConnection = getmBeanServerConnection();
        final MBeanInfo mBeanInfo = mBeanServerConnection.getMBeanInfo(beanName);

        System.out.println("className: "+mBeanInfo.getClassName());

        System.out.println("--------------------------------------");
        final MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
        for (MBeanAttributeInfo attribute : attributes) {
            System.out.println(attribute);
        }

        System.out.println("------------------------------------");
        final MBeanOperationInfo[] operations = mBeanInfo.getOperations();
        for (MBeanOperationInfo operation : operations) {
            System.out.println(operation);
        }

        System.out.println("------------------------------------");
        final MBeanNotificationInfo[] notifications = mBeanInfo.getNotifications();
        for (MBeanNotificationInfo notification : notifications) {
            System.out.println(notification);
        }

    }

    @Test
    public void test13() throws IOException, AttachNotSupportedException {
        VirtualMachine virtualMachine = VirtualMachine.attach("11012");
    }



    void storeClassInfo(final HeapHistogramImpl.ClassInfoImpl newClInfo, final Map<String, HeapHistogramImpl.ClassInfoImpl> map) {
        HeapHistogramImpl.ClassInfoImpl oldClInfo = map.get(newClInfo.getName());
        if (oldClInfo == null) {
            map.put(newClInfo.getName(),newClInfo);
        } else {
            oldClInfo.bytes += newClInfo.getBytes();
            oldClInfo.instances += newClInfo.getInstancesCount();
        }
    }


    private ObjectName createBeanName(String beanName) {
        try {
            return new ObjectName(beanName);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    private MBeanServerConnection getmBeanServerConnection() throws IOException {
        String jmxHostAndPort = "127.0.0.1:10086";
        JMXServiceURL jmxSeriverUrl = new JMXServiceURL("service:jmx:rmi://" + jmxHostAndPort + "/jndi/rmi://" + jmxHostAndPort + "/jmxrmi");
        Map credentials = new HashMap(1);
        String[] creds = new String[]{null, null};
        credentials.put(JMXConnector.CREDENTIALS, creds);
        // 主要耗时方法在获取连接
        JMXConnector connector = JMXConnectorFactory.connect(jmxSeriverUrl,credentials);
        MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
        return mbeanConnection;
    }

    @Test
    public void testpp(){
        System.out.println(int[].class);
        System.out.println(long[].class);
        System.out.println(double[].class);
        System.out.println(float[].class);
        System.out.println(short[].class);
        System.out.println(char[].class);
        System.out.println(byte[].class);
        System.out.println(boolean[].class);
    }

    @Test
    public void testMxBean(){
    }
}

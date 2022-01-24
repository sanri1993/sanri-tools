package com.sanri.tools.modules.jvm.service;

import com.sanri.tools.modules.jvm.service.dtos.HeapHistogram;
import com.sanri.tools.modules.jvm.service.dtos.HeapHistogramImpl;
import com.sanri.tools.modules.jvm.service.dtos.JMXConnectInfo;
import com.sanri.tools.modules.jvm.service.dtos.VMParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class DiagnosticCommandService {
    @Autowired
    private MBeanMonitorService mBeanMonitorService;

    private static final String DIAGNOSTIC_COMMAND_MXBEAN_NAME = "com.sun.management:type=DiagnosticCommand";

    /**
     * 获取堆数据
     * @param jmxConnectInfo
     * @param all
     * @return
     */
    public HeapHistogramImpl gcClassHistogram(String connName, boolean all) throws IOException, MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException {
        final MBeanServerConnection mBeanServerConnection = mBeanMonitorService.loadMBeanConnection(connName);
        ObjectName diagCommName = new ObjectName(DIAGNOSTIC_COMMAND_MXBEAN_NAME);

        String[] signature = new String[]{String[].class.getName()};
        Object [] params = new Object[1];
        params[0] = all ? new String[]{"-all="} : new String[0];
        final String histogramText = (String) mBeanServerConnection.invoke(diagCommName, "gcClassHistogram", params, signature);
        final HeapHistogramImpl heapHistogram = new HeapHistogramImpl(histogramText);
        Comparator<HeapHistogram.ClassInfo> comparator = (a, b) -> {
            long temp =  b.getBytes() - a.getBytes();
            if (temp == 0 ){
                return (int) (b.getInstancesCount() - a.getInstancesCount() );
            }
            return (int) temp;
        };
        Collections.sort(heapHistogram.getHeapHistogram(), comparator);
        return heapHistogram;
    }

    /**
     * 已经设置的 vm 参数
     * @param connName
     */
    public List<VMParam> flagsSetted(String connName) throws IOException, MBeanException, InstanceNotFoundException, ReflectionException {
        final MBeanServerConnection mBeanServerConnection = mBeanMonitorService.loadMBeanConnection(connName);
        final ObjectName diagCommName = mBeanMonitorService.createBeanName(DIAGNOSTIC_COMMAND_MXBEAN_NAME);
        String[] signature = new String[]{String[].class.getName()};
        Object [] params = new Object[1];
        params[0] = new String[0];
        final String vmFlags = (String) mBeanServerConnection.invoke(diagCommName, "vmFlags", params, signature);

        List<VMParam> vmParams = new ArrayList<>();
        final String[] keyValues = StringUtils.split(vmFlags, " ");
        for (String keyValue : keyValues) {
            String realKeyValue = keyValue.trim();
            if (StringUtils.isBlank(realKeyValue)){
                continue;
            }
            final String[] split = StringUtils.split(realKeyValue, "=");
            if (split.length == 2) {
                vmParams.add(new VMParam(split[0], split[1]));
            }else if (split.length == 1){
                vmParams.add(new VMParam(split[0],"true"));
            }else{
                log.warn("vm 参数个数不正确: {}",realKeyValue);
            }
        }
        return vmParams;
    }

    public void flags(String connName) throws IOException, MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException {
        final MBeanServerConnection mBeanServerConnection = mBeanMonitorService.loadMBeanConnection(connName);
        ObjectName diagCommName = new ObjectName(DIAGNOSTIC_COMMAND_MXBEAN_NAME);

        String[] signature = new String[]{String[].class.getName()};
        Object [] params = new Object[1];
        params[0] = new String[]{"-all="};
        // 查询所有的标志
        final String flagsText = (String) mBeanServerConnection.invoke(diagCommName, "vmFlags", params, signature);

        // 查询当前配置的标志
        params[0] = new String[0];
        final String editFlags = (String) mBeanServerConnection.invoke(diagCommName, "vmFlags", params, signature);
    }
}

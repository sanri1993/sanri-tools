package com.sanri.tools.modules.dubbo.service;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.fastjson.JSON;
import com.sanri.tools.modules.core.dtos.PluginDto;
import com.sanri.tools.modules.core.service.classloader.ClassloaderService;
import com.sanri.tools.modules.core.service.plugin.PluginManager;
import com.sanri.tools.modules.dubbo.DubboProviderDto;
import com.sanri.tools.modules.dubbo.dtos.DubboInvokeParam;
import com.sanri.tools.modules.dubbo.dtos.DubboLoadMethodParam;
import com.sanri.tools.modules.dubbo.dtos.MethodInfo;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.zookeeper.service.ZookeeperService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class MainDubboService {

    @Autowired
    private ZookeeperService zookeeperService;
    @Autowired
    private ClassloaderService classloaderService;
    @Autowired
    private PluginManager pluginManager;

    /**
     * 这个主要从 zookeeper 上取有哪些服务
     * 依赖于 zookeeper
     * @param connName
     * @return
     */
    public List<String> services(String connName) throws IOException {
        List<String> childrens = zookeeperService.childrens(connName, "/dubbo");
        return childrens;
    }

    /**
     * 从 zookeeper 上获取,当前服务有哪些提供者
     * @param connName
     * @param serviceName
     * @return
     * @throws IOException
     */
    public List<DubboProviderDto> providers(String connName, String serviceName) throws IOException {
        List<DubboProviderDto> dubboProviderDtos = new ArrayList<>();

        List<String> childrens = zookeeperService.childrens(connName, "/dubbo/" + serviceName+"/providers");
        for (String children : childrens) {
            String decode = URLDecoder.decode(children, StandardCharsets.UTF_8.name());
            URL url = URL.valueOf(decode);
            String address = url.getAddress();
            String serviceInterface = url.getServiceInterface();
            String methods = url.getParameter("methods");
            String group = url.getParameter("group");
            String version = url.getParameter("version");
            String dubbo = url.getParameter("dubbo");
            long timestamp = url.getParameter("timestamp",System.currentTimeMillis());
            String application = url.getParameter("application");
            DubboProviderDto dubboProviderDto = new DubboProviderDto(url.toString(),address);
            dubboProviderDto.config(serviceInterface,group,version,methods,dubbo,timestamp,application);

            dubboProviderDtos.add(dubboProviderDto);
        }

        return dubboProviderDtos;
    }

    /**
     * 获取调用方法详情
     * 主要是要获取参数, 让使用者输入参数
     * @param dubboInvokeParam
     * @return
     */
    public List<MethodInfo> methods(DubboLoadMethodParam dubboInvokeParam) throws ClassNotFoundException {
        String classloaderName = dubboInvokeParam.getClassloaderName();
        String serviceClassName = dubboInvokeParam.getServiceClassName();

        ClassLoader classloader = classloaderService.getClassloader(classloaderName);
        if (classloader == null){
            classloader = ClassLoader.getSystemClassLoader();
        }
        Class<?> clazz = classloader.loadClass(serviceClassName);

        // 获取指定方法
        Method[] declaredMethods = clazz.getDeclaredMethods();
        Map<String, List<Method>> methodMap = Arrays.stream(declaredMethods).collect(Collectors.groupingBy(Method::getName));
        List<MethodInfo> methodInfos = new ArrayList<>();
        String[] methodArray = dubboInvokeParam.methodArray();
        for (String methodName : methodArray) {
            List<Method> methodList = methodMap.get(methodName);
            if (CollectionUtils.isNotEmpty(methodList)){
                for (Method method : methodList) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Class<?> returnType = method.getReturnType();
                    List<String> parameterTypeNames = Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.toList());
                    MethodInfo methodInfo = new MethodInfo(methodName,parameterTypeNames,returnType.getName());
                    methodInfos.add(methodInfo);
                }
            }
        }

        return methodInfos;
    }

    private String [] primitiveTypeNames = {"long"};

    public Object invoke(DubboInvokeParam dubboInvokeParam) throws ClassNotFoundException, NoSuchMethodException, RemotingException, ExecutionException, InterruptedException {
        String classloaderName = dubboInvokeParam.getClassloaderName();
        String serviceClassName = dubboInvokeParam.getServiceName();

        // 解析出 class
        ClassLoader classloader = classloaderService.getClassloader(classloaderName);
        if (classloader == null){
            classloader = ClassLoader.getSystemClassLoader();
        }
        Class<?> clazz = classloader.loadClass(serviceClassName);

        // 解决出方法
        MethodInfo methodInfo = dubboInvokeParam.getMethodInfo();
        List<String> parameterTypeNames = methodInfo.getParameterTypeNames();
        Class[] parameterTypes = new Class[parameterTypeNames.size()];
        for (int i = 0; i < parameterTypeNames.size(); i++) {
            String typeName = parameterTypeNames.get(i);
            if (ArrayUtils.contains(primitiveTypeNames,typeName)){
                if ("long".equals(typeName)){
                    parameterTypes [i] = Long.TYPE;
                    continue;
                }
            }
            parameterTypes [i] = classloader.loadClass(typeName);
        }
        Method method = clazz.getDeclaredMethod(methodInfo.getName(),parameterTypes);

        // 解析参数
        List<String> args = dubboInvokeParam.getArgs();
        Object [] argArray = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object object = JSON.parseObject(args.get(i), parameterTypes[i]);
            argArray[i] = object;
        }

        // 得到要请求的提供者信息
        String providerURL = dubboInvokeParam.getProviderURL();
        URL provider = URL.valueOf(providerURL);
        provider = provider.addParameter(Constants.CODEC_KEY, "dubbo");

        // 请求体封装
        HashMap<String, String> map = getAttachmentFromUrl(provider);
        Request request = new Request();
        request.setVersion(provider.getParameter("version"));
        request.setTwoWay(true);
        request.setData(new RpcInvocation(method, argArray,map));

        // 请求数据
        DoeClient client = new DoeClient(provider);
        client.doConnect();
        client.send(request);
        CompletableFuture<RpcResult> future = ResponseDispatcher.getDispatcher().getFuture(request);
        RpcResult rpcResult = future.get();
        ResponseDispatcher.getDispatcher().removeFuture(request);
        return rpcResult.getValue();
    }

    public static HashMap<String,String> getAttachmentFromUrl(URL url) {

        String interfaceName = url.getParameter(Constants.INTERFACE_KEY, "");
        if (StringUtils.isEmpty(interfaceName)) {
            throw new ToolException("找不到接口名称！");
        }

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.PATH_KEY, interfaceName);
        map.put(Constants.VERSION_KEY, url.getParameter(Constants.VERSION_KEY));
        map.put(Constants.GROUP_KEY, url.getParameter(Constants.GROUP_KEY));
        /**
         *  doesn't necessary to set these params.
         *
         map.put(Constants.SIDE_KEY, Constants.CONSUMER_SIDE);
         map.put(Constants.DUBBO_VERSION_KEY, Version.getVersion());
         map.put(Constants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
         map.put(Constants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
         map.put(Constants.METHODS_KEY, methodNames);
         map.put(Constants.INTERFACE_KEY, interfaceName);
         map.put(Constants.VERSION_KEY, "1.0"); // 不能设置这个，不然服务端找不到invoker
         */
        return map;
    }

    @PostConstruct
    public void register(){
        pluginManager.register(PluginDto.builder().module("dubbo").name("main").build());
    }
}

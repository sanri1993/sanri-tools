package com.sanri.tools.modules.dubbo.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.sanri.tools.modules.classloader.MethodService;
import com.sanri.tools.modules.classloader.dtos.ClassMethodInfo;
import com.sanri.tools.modules.classloader.dtos.InvokeMethodResponse;
import com.sanri.tools.modules.classloader.dtos.MethodReq;
import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.classloader.ClassloaderService;

import com.sanri.tools.modules.dubbo.DubboProviderDto;
import com.sanri.tools.modules.dubbo.dtos.DubboInvokeParam;
import com.sanri.tools.modules.zookeeper.service.ZookeeperService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MainDubboService {

    @Autowired
    private ZookeeperService zookeeperService;
    @Autowired
    private MethodService methodService;

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
     * @param dubboInvokeParam
     * @return
     */
    public InvokeMethodResponse invoke(DubboInvokeParam dubboInvokeParam) throws ClassNotFoundException, NoSuchMethodException, RemotingException, ExecutionException, InterruptedException {
        final MethodReq methodReq = dubboInvokeParam.getMethodReq();
        final Method method = methodService.toMethod(methodReq.getClassloaderName(), methodReq.getClassName(), methodReq.getMethodSignature());
        final Object[] argArray = methodService.convertToMethodParams(methodReq, dubboInvokeParam.getParams());

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

        final ClassMethodInfo classMethodInfo = methodService.toMethodInfo(method);
        // 请求数据
        long startTime = System.currentTimeMillis();
        DoeClient client = new DoeClient(provider);
        client.doConnect();
        client.send(request);
        CompletableFuture<RpcResult> future = ResponseDispatcher.getDispatcher().getFuture(request);
        RpcResult rpcResult = future.get();
        ResponseDispatcher.getDispatcher().removeFuture(request);
        final Object value = rpcResult.getValue();
        long spendTime = System.currentTimeMillis() - startTime;

        return new InvokeMethodResponse(spendTime,classMethodInfo.getReturnType(),value);
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
}

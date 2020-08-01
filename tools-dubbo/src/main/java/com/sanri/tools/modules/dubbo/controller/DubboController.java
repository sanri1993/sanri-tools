package com.sanri.tools.modules.dubbo.controller;

import com.alibaba.dubbo.remoting.RemotingException;
import com.sanri.tools.modules.dubbo.DubboProviderDto;
import com.sanri.tools.modules.dubbo.dtos.DubboInvokeParam;
import com.sanri.tools.modules.dubbo.dtos.DubboLoadMethodParam;
import com.sanri.tools.modules.dubbo.dtos.MethodInfo;
import com.sanri.tools.modules.dubbo.service.MainDubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/dubbo")
public class DubboController {
    @Autowired
    private MainDubboService dubboService;

    /**
     * 所有的 dubbo 服务,在某个连接上
     * @param connName
     * @return
     */
    @GetMapping("/services")
    public List<String> services(String connName) throws IOException {
        return dubboService.services(connName);
    }

    /**
     * 某个服务的提供者列表
     * @param connName
     * @param serviceName
     * @return
     */
    @GetMapping("/providers")
    public List<DubboProviderDto> providers(String connName, String serviceName) throws IOException {
        return dubboService.providers(connName,serviceName);
    }

    /**
     * 某个服务,某个提供者 提供的方法列表详情
     * @param provider
     * @param connName
     * @param serviceName
     * @return
     */
    @GetMapping("/methods")
    public List<MethodInfo> methods(String connName, String serviceName, String methods, String classloaderName) throws ClassNotFoundException {
        DubboLoadMethodParam dubboLoadMethodParam = new DubboLoadMethodParam(connName,serviceName,methods,classloaderName);
        return dubboService.methods(dubboLoadMethodParam);
    }

    /**
     * 调用 dubbo 服务
     * @param dubboInvokeParam
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws RemotingException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @PostMapping("/invoke")
    public Object invoke(@RequestBody DubboInvokeParam dubboInvokeParam) throws ClassNotFoundException, NoSuchMethodException, RemotingException, InterruptedException, ExecutionException {
        return dubboService.invoke(dubboInvokeParam);
    }
}

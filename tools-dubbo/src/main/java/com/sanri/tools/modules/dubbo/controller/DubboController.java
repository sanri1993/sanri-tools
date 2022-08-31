package com.sanri.tools.modules.dubbo.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.alibaba.dubbo.remoting.RemotingException;
import com.sanri.tools.modules.dubbo.DubboProviderDto;
import com.sanri.tools.modules.dubbo.dtos.DubboInvokeParam;
import com.sanri.tools.modules.dubbo.service.MainDubboService;

@RestController
@RequestMapping("/dubbo")
@Validated
public class DubboController {
    @Autowired
    private MainDubboService dubboService;

    /**
     * 某个服务的提供者列表
     * @param connName
     * @param serviceName
     * @return
     */
    @GetMapping("/providers")
    public List<DubboProviderDto> providers(@NotNull String connName, @NotNull String serviceName) throws IOException {
        return dubboService.providers(connName,serviceName);
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
    public Object invoke(@RequestBody @Valid DubboInvokeParam dubboInvokeParam) throws ClassNotFoundException, NoSuchMethodException, RemotingException, InterruptedException, ExecutionException {
        return dubboService.invoke(dubboInvokeParam);
    }
}

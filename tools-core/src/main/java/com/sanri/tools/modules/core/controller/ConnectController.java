package com.sanri.tools.modules.core.controller;

import com.alibaba.fastjson.JSONObject;
import com.sanri.tools.modules.core.service.ConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/connect")
public class ConnectController {
    @Autowired
    private ConnectService connectService;

    /**
     * 模块列表
     * @return
     */
    @GetMapping("/modules")
    public List<String> modules(){
        return connectService.modules();
    }

    /**
     * 指定模块下的连接列表
     * @param module
     * @return
     */
    @GetMapping("/{module}/names")
    public Set<String> names(@PathVariable("module") String module){
        return connectService.names(module);
    }

    /**
     * 获取连接详情
     * @param module
     * @param connName
     * @return
     */
    @GetMapping("/{module}/{connName}")
    public String content(@PathVariable("module") String module,@PathVariable("connName")String connName) throws IOException {
        return connectService.content(module,connName);
    }

    /**
     * 创建连接
     * @param module
     * @param data 动态数据 ; {@link  com.sanri.tools.modules.protocol.param.AbstractConnectParam}
     * @throws IOException
     */
    @PostMapping("/create/{module}")
    public void createDatabase(@PathVariable("module") String module,@RequestBody JSONObject data) throws IOException {
        connectService.createConnect(module,data.toJSONString());
    }


}

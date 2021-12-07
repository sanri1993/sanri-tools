package com.sanri.tools.modules.core.controller.security;

import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectInput;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/security/connect")
public class SecurityConnectController {
    private final ConnectService connectService;

    public SecurityConnectController(ConnectService connectService) {
        this.connectService = connectService;
    }

    /**
     * 模块列表
     */
    @GetMapping("/modules")
    public List<String> modules(){
        return connectService.modules();
    }

    /**
     * 创建一个模块
     * @param name 模块名
     */
    @PostMapping("/createModule")
    public void createModule(@NotBlank String name){connectService.createModule(name);}

    /**
     * 所有可以访问的连接列表
     */
    @GetMapping("/connects")
    public List<ConnectOutput> connects(){
        return connectService.connects();
    }

    /**
     * 模块可访问连接列表
     * @param module 模块名
     */
    @GetMapping("/moduleConnects")
    public List<ConnectOutput> moduleConnects(@NotBlank String module){
        return connectService.moduleConnects(module);
    }

    /**
     * 连接配置详情
     * @param module 模块名
     * @param baseName 基础名
     */
    @GetMapping("/loadContent")
    public String loadContent(@NotBlank String module,@NotBlank String baseName) throws IOException {
        return connectService.loadContent(module,baseName);
    }

    /**
     * 写入配置
     * @param connectInput 配置信息
     */
    @PostMapping("/writeConfig")
    public void writeConfig(@RequestBody @Validated ConnectInput connectInput) throws IOException {
        connectService.updateConnect(connectInput);
    }

}

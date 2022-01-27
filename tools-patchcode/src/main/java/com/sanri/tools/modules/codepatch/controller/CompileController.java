package com.sanri.tools.modules.codepatch.controller;

import com.sanri.tools.modules.codepatch.controller.dtos.CompileMessage;
import com.sanri.tools.modules.codepatch.service.GitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class CompileController {

    @Autowired
    private GitService gitService;

    /**
     * 收到编译命令, 然后发起编译
     * @param compileMessage
     */
    @MessageMapping("compile")
    public void compile(CompileMessage compileMessage){
        log.info("收到消息:{}",compileMessage);
    }
}

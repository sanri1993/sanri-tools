package com.sanri.tools.modules.codepatch.service;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sanri.tools.modules.core.security.UserService;
import com.sanri.tools.modules.core.security.dtos.ThinUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sanri.tools.modules.codepatch.controller.dtos.CompileMessage;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class GitWebsocketEndpoint {

    @Autowired
    private GitService gitService;

    @Autowired(required = false)
    private UserService userService;

    @MessageMapping("/compile")
    public void compile(@RequestBody CompileMessage compileMessage, SimpMessageHeaderAccessor simpMessageHeaderAccessor) throws IOException, InterruptedException {
        final InetSocketAddress ip = (InetSocketAddress)simpMessageHeaderAccessor.getSessionAttributes().get("ip");
        gitService.compile(ip.getHostName(),compileMessage.getWebsocketId(),compileMessage.getGroup(),compileMessage.getRepository(),compileMessage.getRelativePath());
    }
}

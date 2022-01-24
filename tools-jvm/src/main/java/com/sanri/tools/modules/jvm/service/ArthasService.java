package com.sanri.tools.modules.jvm.service;

import com.alibaba.fastjson.JSON;
import com.google.common.net.HostAndPort;
import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.jvm.service.dtos.CommandResultContext;
import com.sanri.tools.modules.jvm.service.dtos.JMXConnectInfo;
import com.sanri.tools.modules.jvm.service.handlers.NoHandleCommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ArthasService {

    @Autowired
    private ConnectService connectService;

    /**
     *  arthas 客户端列表 connName => ArthasClient
     */
    private Map<String,ArthasClient> arthasConnectMap = new ConcurrentHashMap<>();

    /**
     * 连接到 arthas
     * @param connName jmx 连接名
     * @param port  arthas 开放的端口
     */
    public void linkArthas(String connName,int port) throws IOException {
        final String connectInfo = connectService.loadContent("jvm", connName);
        final JMXConnectInfo jmxConnectInfo = JSON.parseObject(connectInfo, JMXConnectInfo.class);
        final HostAndPort hostAndPort = HostAndPort.fromString(jmxConnectInfo.getJmxHostAndPort());
        final ArthasClient arthasClient = new ArthasClient(hostAndPort.getHost(), port);
        arthasConnectMap.put(connName,arthasClient);
    }

    /**
     * 发送一个命令, 得到结果
     * @param connName
     * @param command
     * @return
     */
    public Object sendCommand(String connName, String command) throws IOException {
        final ArthasClient arthasClient = arthasConnectMap.get(connName);
        final String sendCommand = arthasClient.sendCommand(command);

        final CommandResultContext commandResultContext = new CommandResultContext(sendCommand);
        ArthasCommandHandler arthasCommandHandler = new NoHandleCommandHandler();
        arthasCommandHandler.process(commandResultContext);
        return commandResultContext.getResult();
    }
}

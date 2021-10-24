package com.sanri.tools.modules.codepatch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/compile/message/{id}")
@Component
public class WebSocketService {
    private static final Map<String,Session> sessionMap = new ConcurrentHashMap<>();
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session,@PathParam("id") String id) {
        log.info("打开 session 连接:{}",id);
        sessionMap.put(id,session);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("id") String id) {
        log.info("关闭 session 连接:{}",id);
        sessionMap.remove(id);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误",error);
    }

    /**
     * 服务端发送消息给客户端
     */
    public void sendMessage(String id,String message) {
        final Session session = sessionMap.get(id);
        if (session != null && session.isOpen()) {
            try {
                log.info("服务端给客户端[{}]发送消息{}", id, message);
                synchronized (session) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error("服务端发送消息给客户端失败：{}", e);
            }
        }else{
            log.error("session 已经关闭, 发送消息失败:{}",message);
        }

    }
}

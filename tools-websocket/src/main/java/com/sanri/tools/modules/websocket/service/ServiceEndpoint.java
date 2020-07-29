package com.sanri.tools.modules.websocket.service;

import com.sanri.tools.modules.websocket.service.messages.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint("/listen/{sessionId}")
@Component
@Slf4j
public class ServiceEndpoint {
    private Map<String,WebSocketClient> clientMap = new HashMap<>();

    @Autowired
    private MessageDispatch messageDispatch;

    @OnOpen
    public void onOpen(Session session , @PathParam("sessionId") String sesionId){
        clientMap.put(sesionId,new WebSocketClient(session));
    }

    @OnClose
    public void onClose(Session session,@PathParam("sessionId") String sessionId){
        clientMap.remove(sessionId);
    }

    @OnMessage
    public void onMessage(String message,Session session,@PathParam("sessionId") String sessionId){
        ClientMessage clientMessage = new ClientMessage("",System.currentTimeMillis(),null);
        WebSocketClient webSocketClient = clientMap.get(sessionId);
        messageDispatch.onMessage(clientMessage,webSocketClient);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误：{}，Session ID： {}",error.getMessage(),session.getId());
        error.printStackTrace();
    }
}

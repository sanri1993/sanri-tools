package com.sanri.tools.modules.websocket.service;

import com.sanri.tools.modules.websocket.service.messages.ClientMessage;

public interface WebsocketMessageListener {
    /**
     * 客户端上报消息
     * @param clientMessage
     * @param webSocketClient
     */
    void listen(ClientMessage clientMessage,WebSocketClient webSocketClient);

    /**
     * 支持的消息
     * @param routingKey
     * @return
     */
    boolean support(String routingKey);
}

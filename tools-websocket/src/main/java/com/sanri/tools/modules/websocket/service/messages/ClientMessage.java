package com.sanri.tools.modules.websocket.service.messages;

import lombok.Data;

@Data
public class ClientMessage {
    // 路由位置,一般用于模块消息
    private String routingKey;
    private long timestamp;
    private Object payload;

    public ClientMessage(String routingKey, long timestamp, Object payload) {
        this.routingKey = routingKey;
        this.timestamp = timestamp;
        this.payload = payload;
    }
}

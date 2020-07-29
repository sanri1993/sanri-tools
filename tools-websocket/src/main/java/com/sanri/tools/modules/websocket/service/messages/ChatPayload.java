package com.sanri.tools.modules.websocket.service.messages;

import lombok.Data;

/**
 * 聊天消息
 */
@Data
public class ChatPayload {
    private byte [] message;
    private int messageType;
    private long from;
    private long to;
}

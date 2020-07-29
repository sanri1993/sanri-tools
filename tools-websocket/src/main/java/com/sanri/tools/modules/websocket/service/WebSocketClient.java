package com.sanri.tools.modules.websocket.service;

import lombok.Data;
import lombok.Getter;

import javax.websocket.Session;

@Getter
public class WebSocketClient {
    private Session session;

    public WebSocketClient(Session session) {
        this.session = session;
    }
}

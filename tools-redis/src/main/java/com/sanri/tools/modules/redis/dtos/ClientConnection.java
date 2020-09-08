package com.sanri.tools.modules.redis.dtos;

import lombok.Data;
import redis.clients.jedis.HostAndPort;

/**
 * redis 的客户端连接
 */
@Data
public class ClientConnection {
    private HostAndPort target;
    private String id;
    private HostAndPort hostAndPort;
    private String age;
    private String idle;
    private String cmd;

    public ClientConnection() {
    }

    public ClientConnection(HostAndPort target) {
        this.target = target;
    }
}

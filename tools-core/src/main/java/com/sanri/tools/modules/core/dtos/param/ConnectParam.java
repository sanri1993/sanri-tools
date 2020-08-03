package com.sanri.tools.modules.core.dtos.param;

import lombok.Data;

@Data
public class ConnectParam {
    private String host;
    private int port;

    public static final int DEFAULT_SESSION_TIMEOUT = 30000;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

    private int sessionTimeout = DEFAULT_SESSION_TIMEOUT;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    /**
     * 获取连接字符串
     * @return
     */
    public String getConnectString(){
        return host+":"+port;
    }
}

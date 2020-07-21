package com.sanri.tools.modules.protocol.param;

import lombok.Data;

@Data
public class ConnectParam {
    private String host;
    private int port;

    /**
     * 获取连接字符串
     * @return
     */
    public String getConnectString(){
        return host+":"+port;
    }
}

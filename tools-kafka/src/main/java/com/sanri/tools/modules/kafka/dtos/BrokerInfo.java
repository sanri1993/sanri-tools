package com.sanri.tools.modules.kafka.dtos;

import lombok.Data;

@Data
public class BrokerInfo {
    private int id;
    private String host;
    private int port;
    private int jxmPort;

    public BrokerInfo() {
    }

    public BrokerInfo(int id, String host, int port, int jxmPort) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.jxmPort = jxmPort;
    }

}

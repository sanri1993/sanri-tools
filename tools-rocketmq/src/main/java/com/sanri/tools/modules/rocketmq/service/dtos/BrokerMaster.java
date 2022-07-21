package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;

@Data
public class BrokerMaster {
    private String brokerName;
    private String addr;

    public BrokerMaster() {
    }

    public BrokerMaster(String brokerName, String addr) {
        this.brokerName = brokerName;
        this.addr = addr;
    }
}

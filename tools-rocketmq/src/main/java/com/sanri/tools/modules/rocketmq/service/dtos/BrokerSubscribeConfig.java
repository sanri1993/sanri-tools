package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;
import org.apache.rocketmq.common.subscription.SubscriptionGroupConfig;

@Data
public class BrokerSubscribeConfig {
    private BrokerMaster brokerMaster;
    private SubscriptionGroupConfig subscriptionGroupConfig;

    public BrokerSubscribeConfig() {
    }

    public BrokerSubscribeConfig(BrokerMaster brokerMaster, SubscriptionGroupConfig subscriptionGroupConfig) {
        this.brokerMaster = brokerMaster;
        this.subscriptionGroupConfig = subscriptionGroupConfig;
    }
}

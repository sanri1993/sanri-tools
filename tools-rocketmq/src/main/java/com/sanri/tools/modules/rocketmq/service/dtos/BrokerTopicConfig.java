package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;
import org.apache.rocketmq.common.TopicConfig;

@Data
public class BrokerTopicConfig {
    private BrokerMaster brokerMaster;
    private TopicConfig topicConfig;

    public BrokerTopicConfig() {
    }

    public BrokerTopicConfig(BrokerMaster brokerMaster, TopicConfig topicConfig) {
        this.brokerMaster = brokerMaster;
        this.topicConfig = topicConfig;
    }
}

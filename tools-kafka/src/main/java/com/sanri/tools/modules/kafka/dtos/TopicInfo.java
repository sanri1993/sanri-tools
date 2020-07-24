package com.sanri.tools.modules.kafka.dtos;

import lombok.Data;

@Data
public class TopicInfo {
    private String topic;
    private int partitions;

    public TopicInfo() {
    }

    public TopicInfo(String topic, int partitions) {
        this.topic = topic;
        this.partitions = partitions;
    }
}

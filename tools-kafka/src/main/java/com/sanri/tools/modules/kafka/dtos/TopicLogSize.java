package com.sanri.tools.modules.kafka.dtos;

import lombok.Data;

@Data
public class TopicLogSize {
    private String topic;
    private int partition;
    private long logSize;

    public TopicLogSize() {
    }

    public TopicLogSize(String topic, long logSize) {
        this.topic = topic;
        this.logSize = logSize;
    }

    public TopicLogSize(String topic, int partition, long logSize) {
        this.topic = topic;
        this.partition = partition;
        this.logSize = logSize;
    }
}

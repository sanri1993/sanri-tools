package com.sanri.tools.modules.kafka.dtos;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class SimpleTopicPartition {
    private String topic;
    private int partition;

    public SimpleTopicPartition() {
    }

    public SimpleTopicPartition(String topic, int partition) {
        this.topic = topic;
        this.partition = partition;
    }
}

package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;
import org.apache.rocketmq.common.admin.OffsetWrapper;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConsumerTopic {
    private String topic;
    private List<QueueOffset> queueOffsets = new ArrayList<>();

    public ConsumerTopic() {
    }

    public ConsumerTopic(String topic) {
        this.topic = topic;
    }

    @Data
    public static final class QueueOffset{
        private String brokerName;
        private int queueId;
        private OffsetWrapper offsetWrapper;

        public QueueOffset() {
        }

        public QueueOffset(String brokerName, int queueId, OffsetWrapper offsetWrapper) {
            this.brokerName = brokerName;
            this.queueId = queueId;
            this.offsetWrapper = offsetWrapper;
        }
    }
}

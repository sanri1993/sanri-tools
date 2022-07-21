package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;
import org.apache.rocketmq.common.admin.TopicOffset;

import java.util.ArrayList;
import java.util.List;

@Data
public class TopicInfo {
    private String topic;
    private List<QueueInfo> queueInfos = new ArrayList<>();

    public TopicInfo() {
    }

    public TopicInfo(String topic) {
        this.topic = topic;
    }

    @Data
    public static final class QueueInfo{
        private String brokerName;
        private int queueId;
        private TopicOffset topicOffset;

        public QueueInfo() {
        }

        public QueueInfo(String brokerName, int queueId, TopicOffset topicOffset) {
            this.brokerName = brokerName;
            this.queueId = queueId;
            this.topicOffset = topicOffset;
        }
    }
}

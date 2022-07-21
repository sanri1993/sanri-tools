package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;

@Data
public class SendTopicMessageRequest {
    private String topic;
    private String key;
    private String tag;

    private String messageBody;
}

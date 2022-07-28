package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class MessageView {
    private String topic;
    private String body;
    private String brokerName;
    private int queueId;
    private long queueOffset;
    private long bornTimestamp;
    private String offsetMsgId;
}

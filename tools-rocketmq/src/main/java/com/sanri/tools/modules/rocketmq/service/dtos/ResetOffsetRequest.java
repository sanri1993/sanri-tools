package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;

@Data
public class ResetOffsetRequest {
    private String consumerGroup;
    private String topic;
    private long resetTime;
    private boolean force;
}

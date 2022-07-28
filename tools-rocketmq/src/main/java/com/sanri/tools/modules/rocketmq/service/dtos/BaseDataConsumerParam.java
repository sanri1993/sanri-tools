package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;

@Data
public class BaseDataConsumerParam {
    protected String topic;
    protected int queueId;

    protected int limit;
}

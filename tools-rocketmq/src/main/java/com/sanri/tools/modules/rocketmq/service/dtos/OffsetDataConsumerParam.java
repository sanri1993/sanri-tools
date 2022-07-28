package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;

@Data
public class OffsetDataConsumerParam extends BaseDataConsumerParam {
    private long offset;
}

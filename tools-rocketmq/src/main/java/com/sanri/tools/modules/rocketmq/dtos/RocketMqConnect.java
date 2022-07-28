package com.sanri.tools.modules.rocketmq.dtos;

import lombok.Data;

@Data
public class RocketMqConnect {
    private String namesrvAddr;
    private String accessKey;
    private String secretKey;
    private Boolean useTLS;
}

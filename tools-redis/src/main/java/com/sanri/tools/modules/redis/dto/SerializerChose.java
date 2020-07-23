package com.sanri.tools.modules.redis.dto;

import lombok.Data;

@Data
public class SerializerChose {
    private String value;
    private String hashKey;
    private String hashValue;
}
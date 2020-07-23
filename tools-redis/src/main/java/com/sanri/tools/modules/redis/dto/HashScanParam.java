package com.sanri.tools.modules.redis.dto;

import lombok.Data;

@Data
public class HashScanParam extends RedisCommandParam {
    private String pattern;
    private int limit;
    private String hashKey;
    private String hashKeySerizlizer;
}

package com.sanri.tools.modules.redis.dtos;

import lombok.Data;

@Data
public class HashScanParam extends RedisCommandParam {
    private String pattern;
    private int limit;
    private String hashKey;
    private String hashKeySerializer;
    private String cursor = "0";
}

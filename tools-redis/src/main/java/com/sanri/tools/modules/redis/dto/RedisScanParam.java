package com.sanri.tools.modules.redis.dto;

import lombok.Data;

@Data
public class RedisScanParam extends RedisCommandParam {
    private String pattern;
    private int limit;
}

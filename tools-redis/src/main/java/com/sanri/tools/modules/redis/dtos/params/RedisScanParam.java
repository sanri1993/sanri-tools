package com.sanri.tools.modules.redis.dtos.params;

import lombok.Data;

@Data
public class RedisScanParam {
    private String pattern;
    private int limit;
    private String cursor;
    private long timeout = -1;
}

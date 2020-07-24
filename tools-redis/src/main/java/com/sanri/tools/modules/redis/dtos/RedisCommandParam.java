package com.sanri.tools.modules.redis.dtos;

import lombok.Data;

@Data
public class RedisCommandParam {
    protected String connName;
    protected int index;
    private String key;
    private String keySerializer = "string";
}

package com.sanri.tools.modules.redis.dto;

import lombok.Data;

@Data
public class RedisKeyResult {
    private String key;
    private String type;
    //过期时间秒值
    private long ttl;
    //过期时间毫秒值
    private long pttl;
    private long length;

    public RedisKeyResult() {
    }

    public RedisKeyResult(String key, String type) {
        this.key = key;
        this.type = type;
    }

    public RedisKeyResult(String key, String type, long ttl, long pttl) {
        this.key = key;
        this.type = type;
        this.ttl = ttl;
        this.pttl = pttl;
    }
}

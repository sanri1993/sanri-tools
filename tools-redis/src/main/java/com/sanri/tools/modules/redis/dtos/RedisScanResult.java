package com.sanri.tools.modules.redis.dtos;

import lombok.Data;
import redis.clients.jedis.HostAndPort;

import java.util.List;

@Data
public class RedisScanResult {
    /**
     * 扫描出来的 key 列表
     */
    private List<RedisKeyResult> keys;

    /**
     * 最后一个扫描 key 的主机和游标,顺序号
     */
    private HostAndPort hostAndPort;
    private String cursor;
    private int hostIndex;

    public RedisScanResult() {
    }

    public RedisScanResult(List<RedisKeyResult> keys, HostAndPort hostAndPort, String cursor) {
        this.keys = keys;
        this.hostAndPort = hostAndPort;
        this.cursor = cursor;
    }

    public RedisScanResult(List<RedisKeyResult> keys, HostAndPort hostAndPort, String cursor, int hostIndex) {
        this.keys = keys;
        this.hostAndPort = hostAndPort;
        this.cursor = cursor;
        this.hostIndex = hostIndex;
    }
}

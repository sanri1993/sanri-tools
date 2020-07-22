package com.sanri.tools.modules.redis.service;

import lombok.Data;
import redis.clients.jedis.Jedis;

/**
 * 重新包裹一下 jedis ,因为要知道它是集群还是单点
 * 主从的数据是一样的,没有关系
 */
@Data
public class JedisWrap {
    private Jedis jedis;
    private boolean cluster;
    // info 命令的结果
    private String info;
    // 上次调用 info 命令的时间
    private long lastRefreshTime;

    public JedisWrap(Jedis jedis) {
        this.jedis = jedis;
    }
}

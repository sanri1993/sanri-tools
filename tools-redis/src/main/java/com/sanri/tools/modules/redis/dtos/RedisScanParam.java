package com.sanri.tools.modules.redis.dtos;

import lombok.Data;

@Data
public class RedisScanParam extends RedisCommandParam {
    private String pattern;
    private int limit;
    /**
     * 游标格式为 hostIndex|cursor
     * 首次搜索时游标数据为 0 ,没有格式
     */
    private String cursor;
}

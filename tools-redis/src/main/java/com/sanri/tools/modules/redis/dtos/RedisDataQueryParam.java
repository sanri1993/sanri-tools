package com.sanri.tools.modules.redis.dtos;

import lombok.Data;

@Data
public class RedisDataQueryParam extends RedisCommandParam {
    private String classloaderName;
    private SerializerChose serializerChose;
    private ExtraQueryParam extraQueryParam;
}

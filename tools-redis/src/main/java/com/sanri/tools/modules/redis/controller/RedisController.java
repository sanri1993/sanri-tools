package com.sanri.tools.modules.redis.controller;

import com.sanri.tools.modules.redis.dtos.KeyScanResult;
import com.sanri.tools.modules.redis.dtos.params.*;
import com.sanri.tools.modules.redis.service.RedisClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("/redis")
@RestController
public class RedisController {
    @Autowired
    private RedisClusterService redisClusterService;

    @GetMapping("/key/scan")
    public KeyScanResult scan(ConnParam connParam, RedisScanParam scanParam,SerializerParam serializerParam, int hostIndex) throws IOException, ClassNotFoundException {
        return redisClusterService.scan(connParam,scanParam,serializerParam,hostIndex);
    }

    @PostMapping("/key/drop")
    public void dropKeys(ConnParam connParam,String [] keys) throws IOException {
        redisClusterService.dropKeys(connParam,keys);
    }

    /**
     * 适用于类型是 hash , set , zset 类型的
     * @param connParam
     * @param key
     * @param scanParam
     */
    @GetMapping("/key/subKeys")
    public void subKeys(ConnParam connParam, String key, RedisScanParam scanParam){

    }

    @GetMapping("/data")
    public void data(ConnParam connParam, SubKeyParam subKeyParam, RangeParam rangeParam, SerializerParam serializerParam){

    }
}

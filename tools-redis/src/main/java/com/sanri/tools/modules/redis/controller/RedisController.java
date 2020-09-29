package com.sanri.tools.modules.redis.controller;

import com.sanri.tools.modules.redis.dtos.KeyScanResult;
import com.sanri.tools.modules.redis.dtos.SubKeyScanResult;
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
    public KeyScanResult scan(ConnParam connParam, RedisScanParam scanParam,SerializerParam serializerParam) throws IOException, ClassNotFoundException {
        return redisClusterService.scan(connParam,scanParam,serializerParam);
    }

    @PostMapping("/key/drop")
    public void dropKeys(ConnParam connParam,String [] keys) throws IOException {
        redisClusterService.dropKeys(connParam,keys);
    }

    /**
     * 适用于类型是 hash , set , zset 类型的
     * @param scanParam
     * @param connParam
     * @param key
     * @return
     */
    @GetMapping("/key/subKeys")
    public SubKeyScanResult subKeys(ConnParam connParam, String key, RedisScanParam redisScanParam, SerializerParam serializerParam) throws IOException, ClassNotFoundException {
        return redisClusterService.subKeyScan(connParam,key,redisScanParam,serializerParam);
    }

    /**
     * 查询数据
     * @param connParam
     * @param subKeyParam
     * @param rangeParam
     * @param redisScanParam
     * @param serializerParam
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @GetMapping("/data")
    public Object data(ConnParam connParam, SubKeyParam subKeyParam, RangeParam rangeParam, RedisScanParam redisScanParam, SerializerParam serializerParam) throws IOException, ClassNotFoundException {
        return redisClusterService.data(connParam,subKeyParam,rangeParam,redisScanParam,serializerParam);
    }
}

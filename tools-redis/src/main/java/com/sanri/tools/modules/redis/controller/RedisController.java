package com.sanri.tools.modules.redis.controller;

import com.sanri.tools.modules.redis.dtos.HashKeyScanResult;
import com.sanri.tools.modules.redis.dtos.KeyScanResult;
import com.sanri.tools.modules.redis.dtos.in.*;
import com.sanri.tools.modules.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.io.IOException;

@RequestMapping("/redis")
@RestController
@Validated
public class RedisController {
    @Autowired
    private RedisService redisService;

    @GetMapping("/key/scan")
    public KeyScanResult scan(@Validated ConnParam connParam, KeyScanParam keyScanParam, SerializerParam serializerParam) throws IOException, ClassNotFoundException {
        return redisService.scan(connParam,keyScanParam,serializerParam);
    }

    @PostMapping("/key/drop")
    public Long dropKeys(@Validated ConnParam connParam,String [] keys, SerializerParam serializerParam) throws IOException {
        return redisService.dropKeys(connParam,keys,serializerParam);
    }

    /**
     * @param scanParam
     * @param connParam
     * @param key
     * @return
     */
    @GetMapping("/key/hscan")
    public HashKeyScanResult hscan(@Validated ConnParam connParam, HashKeyScanParam hashKeyScanParam, SerializerParam serializerParam) throws IOException, ClassNotFoundException {
        return redisService.hscan(connParam,hashKeyScanParam,serializerParam);
    }

//    @GetMapping("/key/length")
//    public long keyLength(@Validated ConnParam connParam, @NotNull String key, SerializerParam serializerParam) throws IOException {
//        return redisService.keyLength(connParam,key,serializerParam);
//    }

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
    public Object data(@Validated ValueParam valueParam) throws IOException, ClassNotFoundException {
        return redisService.data(valueParam);
    }

    /**
     * 集合操作 , 交(inter),并(union),差(diff)
     * @param connParam
     * @param members
     * @param command
     * @param serializerParam
     * @return
     */
    @GetMapping("/collectionMethods")
    public Object collectionMethods(@Validated ConnParam connParam,String [] members,String command,SerializerParam serializerParam) throws IOException, ClassNotFoundException {
        return redisService.collectionMethods(connParam,members,command,serializerParam);
    }
}

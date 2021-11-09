package com.sanri.tools.modules.redis.controller;

import com.sanri.tools.modules.redis.dtos.HashKeyScanResult;
import com.sanri.tools.modules.redis.dtos.KeyScanResult;
import com.sanri.tools.modules.redis.dtos.TreeKey;
import com.sanri.tools.modules.redis.dtos.in.*;
import com.sanri.tools.modules.redis.service.RedisService;
import com.sanri.tools.modules.redis.service.RedisTreeKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/redis")
@RestController
@Validated
public class RedisController {
    @Autowired
    private RedisService redisService;
    @Autowired
    private RedisTreeKeyService redisTreeKeyService;

    @GetMapping("/key/tree")
    public List<TreeKey> treeKeys(@Validated ConnParam connParam) throws IOException {
        return redisTreeKeyService.treeKeys(connParam);
    }

    /**
     * 查询某个 key 的详细信息
     * @param connParam
     * @param key
     * @return
     */
    @GetMapping("/key/info")
    public KeyScanResult.KeyResult keyInfo(@Validated ConnParam connParam, String key,SerializerParam serializerParam) throws IOException {
        return redisTreeKeyService.keyInfo(connParam,key,serializerParam);
    }

    @GetMapping("/key/del/pattern")
    public long delKeyPattern(@Validated ConnParam connParam,String keyPattern) throws IOException {
        return redisTreeKeyService.dropKeyPattern(connParam, keyPattern);
    }

    @GetMapping("/key/scan")
    public KeyScanResult scan(@Validated ConnParam connParam, KeyScanParam keyScanParam, SerializerParam serializerParam) throws IOException, ClassNotFoundException {
        return redisService.scan(connParam,keyScanParam,serializerParam);
    }

    @PostMapping("/key/del")
    public Long delKeys(@RequestBody DelKeysParam delKeysParam) throws IOException {
        return redisService.delKeys(delKeysParam.getConnParam(),delKeysParam.getKeys(),delKeysParam.getSerializerParam());
    }

    /**
     * @param hashKeyScanParam
     * @param connParam
     * @param serializerParam
     * @return
     */
    @GetMapping("/key/hscan")
    public HashKeyScanResult hscan(@Validated ConnParam connParam, HashKeyScanParam hashKeyScanParam, SerializerParam serializerParam) throws IOException, ClassNotFoundException {
        return redisService.hscan(connParam,hashKeyScanParam,serializerParam);
    }

    /**
     * hash 删除部分 key
     * @param delFieldsParam
     * @return
     */
    @PostMapping("/key/hash/hdel")
    public Long hdel(@RequestBody DelFieldsParam delFieldsParam) throws IOException {
        return redisService.hdel(delFieldsParam);
    }

    /**
     * 查询数据
     * @param valueParam
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @PostMapping("/data")
    public Object data(@RequestBody ValueParam valueParam) throws IOException, ClassNotFoundException {
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

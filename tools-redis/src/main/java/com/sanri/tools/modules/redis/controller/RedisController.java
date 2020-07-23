package com.sanri.tools.modules.redis.controller;

import com.sanri.tools.modules.redis.dto.*;
import com.sanri.tools.modules.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/redis")
public class RedisController {

    @Autowired
    private RedisService redisService;

    /**
     * 获取连接的节点列表
     * @param connName
     * @return
     * @throws IOException
     */
    @GetMapping("/nodes")
    public List<RedisNode> nodes(String connName) throws IOException {
        return redisService.nodes(connName);
    }

    /**
     * 扫描 key 列表
     * @param redisScanParam
     * @return
     * @throws IOException
     */
    @GetMapping("/scan")
    public List<RedisKeyResult> scan(RedisScanParam redisScanParam) throws IOException {
        return redisService.scan(redisScanParam);
    }

    /**
     * 获取 key 数据长度
     * @param redisCommandParam
     * @return
     * @throws IOException
     */
    @GetMapping("/length")
    public Long length(RedisCommandParam redisCommandParam) throws IOException{
        return redisService.queryKeyLength(redisCommandParam);
    }

    /**
     * 扫描 hash key 列表
     * @param hashScanParam
     * @return
     * @throws IOException
     */
    @GetMapping("/hashKeys")
    public List<String> hashKeys(HashScanParam hashScanParam) throws IOException {
        return redisService.hashKeys(hashScanParam);
    }

    @GetMapping("/data")
    public Object data(RedisDataQueryParam dataQueryParam) throws IOException, ClassNotFoundException {
        return redisService.loadData(dataQueryParam);
    }
}

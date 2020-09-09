package com.sanri.tools.modules.redis.controller;

import com.sanri.tools.modules.redis.dtos.*;
import com.sanri.tools.modules.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * redis 比较重要的监控数据为:
 * 查看当前的模式 单机,主从树状结构,集群(每个节点的槽位信息)            已实现
 * 查看内存使用                                                 已实现
 * 查看连接数,哪些主机占用多少连接                                 已实现
 * 模糊搜索某个 key ,查看 key 的数据,注意集群模式下 key 的搜索       已实现
 * 批量删除 key ,先模糊查询,可以指定前缀删除                        还没实现
 */
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

    @GetMapping("/memoryUses")
    public List<MemoryUse> memoryUses(String connName) throws IOException {
        return redisService.memoryUse(connName);
    }

    @GetMapping("/clientList")
    public List<ClientConnection> clientList(String connName) throws IOException {
        List<ClientConnection> clientConnections = redisService.clientList(connName);
        return clientConnections;
    }

    /**
     * 扫描 key 列表
     * @param redisScanParam
     * @return
     * @throws IOException
     */
    @GetMapping("/scan")
    public RedisScanResult scan(RedisScanParam redisScanParam) throws IOException, ClassNotFoundException {
        return redisService.scan(redisScanParam);
    }

    /**
     * 获取 key 数据长度
     * @param redisCommandParam
     * @return
     * @throws IOException
     */
    @GetMapping("/keyLength")
    public Long keyLength(RedisCommandParam redisCommandParam) throws IOException{
        return redisService.queryKeyLength(redisCommandParam);
    }

    /**
     * 扫描 hash key 列表
     * @param hashScanParam
     * @return
     * @throws IOException
     */
    @GetMapping("/hashKeyScan")
    public HashKeyScanResult hashKeyScan(HashScanParam hashScanParam) throws IOException, ClassNotFoundException {
        return redisService.hashKeyScan(hashScanParam);
    }

    /**
     * 获取数据,并使用指定的序列化方式序列化值
     * @param dataQueryParam
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @PostMapping("/data")
    public Object data(@RequestBody RedisDataQueryParam dataQueryParam) throws IOException, ClassNotFoundException {
        return redisService.loadData(dataQueryParam);
    }
}

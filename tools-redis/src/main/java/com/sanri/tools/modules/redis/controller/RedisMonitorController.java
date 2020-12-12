package com.sanri.tools.modules.redis.controller;

import com.sanri.tools.modules.redis.dtos.ClientConnection;
import com.sanri.tools.modules.redis.dtos.MemoryUse;
import com.sanri.tools.modules.redis.dtos.RedisNode;
import com.sanri.tools.modules.redis.dtos.RedisSlowlog;
import com.sanri.tools.modules.redis.dtos.params.ConnParam;
import com.sanri.tools.modules.redis.service.RedisClusterService;
import com.sanri.tools.modules.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/redis/monitor")
@RestController
public class RedisMonitorController {
    @Autowired
    private RedisClusterService redisClusterService;
    @Autowired
    private RedisService redisService;

    /**
     * 运行模式查询 standalone , master-slave, cluster
     * @param connParam
     * @return
     * @throws IOException
     */
    @GetMapping("/mode")
    public String mode(ConnParam connParam) throws IOException {
        return redisClusterService.mode(connParam);
    }

    /**
     * 非集群模式下, 查看节点的数据库数量
     * @param connParam
     * @return
     * @throws IOException
     */
    @GetMapping("/dbs")
    public long dbs(ConnParam connParam) throws IOException {
        return redisService.dbs(connParam);
    }

    /**
     * redis 节点列表
     * @param connParam
     * @return
     * @throws IOException
     */
    @GetMapping("/nodes")
    public List<RedisNode> nodes(ConnParam connParam) throws IOException {
        return redisClusterService.nodes(connParam);
    }

    /**
     * 各节点内存使用情况查询
     * @param connParam
     * @return
     * @throws IOException
     */
    @GetMapping("/memoryUses")
    public List<MemoryUse> memoryUses(ConnParam connParam) throws IOException {
        return redisClusterService.memoryUses(connParam);
    }

    /**
     * 客户端连接列表
     * @param connParam
     * @return
     * @throws IOException
     */
    @GetMapping("/clientList")
    public List<ClientConnection> clientList(ConnParam connParam) throws IOException {
        return redisClusterService.clientList(connParam);
    }

    /**
     * kill 某一个客户端
     * @param connParam
     * @param clientId
     * @return
     * @throws IOException
     */
    @PostMapping("/client/kill/{clientId}")
    public String killClient(ConnParam connParam, @PathVariable("clientId") String clientId) throws IOException {
        return redisService.killClient(connParam,clientId);
    }

    /**
     * 查询 redis 慢查询
     * @param connParam
     * @return
     */
    @GetMapping("/slowlogs")
    public List<RedisSlowlog> redisSlowlogs(ConnParam connParam) throws IOException {
        return redisClusterService.slowlogs(connParam);
    }
}

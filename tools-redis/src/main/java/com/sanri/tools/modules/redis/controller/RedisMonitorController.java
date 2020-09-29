package com.sanri.tools.modules.redis.controller;

import com.sanri.tools.modules.redis.dtos.ClientConnection;
import com.sanri.tools.modules.redis.dtos.MemoryUse;
import com.sanri.tools.modules.redis.dtos.RedisNode;
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

    @GetMapping("/nodes")
    public List<RedisNode> nodes(ConnParam connParam) throws IOException {
        return redisClusterService.nodes(connParam);
    }
    @GetMapping("/memoryUses")
    public List<MemoryUse> memoryUses(ConnParam connParam) throws IOException {
        return redisClusterService.memoryUses(connParam);
    }

    @GetMapping("/clientList")
    public List<ClientConnection> clientList(ConnParam connParam) throws IOException {
        return redisClusterService.clientList(connParam);
    }

    @PostMapping("/client/kill/{clientId}")
    public String killClient(ConnParam connParam, @PathVariable("clientId") String clientId) throws IOException {
        return redisService.killClient(connParam,clientId);
    }
}

package com.sanri.tools.modules.redis.controller;

import com.sanri.tools.modules.redis.dtos.ClientConnection;
import com.sanri.tools.modules.redis.dtos.MemoryUse;
import com.sanri.tools.modules.redis.dtos.RedisNode;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/redis/monitor")
@RestController
public class RedisMonitorController {

    @GetMapping("/nodes")
    public List<RedisNode> nodes(String connName){
        return null;
    }
    @GetMapping("/memoryUses")
    public List<MemoryUse> memoryUses(String connName){
        return null;
    }

    @GetMapping("/clientList")
    public List<ClientConnection> clientList(String connName) throws IOException {
        return null;
    }

    @PostMapping("/client/kill/{clientId}")
    public void killClient(String connName,@PathVariable("clientId") String clientId){

    }
}

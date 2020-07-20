package com.sanri.tools.modules.redis;

import com.sanri.tools.modules.core.service.ConnectService;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.protocol.param.ConnectIdParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RedisService {
    @Autowired
    ConnectService connectService;

    Map<ConnectIdParam, RedisProperties.Jedis> connectMap = new HashMap<>();

}

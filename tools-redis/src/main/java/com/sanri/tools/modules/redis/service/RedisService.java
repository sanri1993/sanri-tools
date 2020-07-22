package com.sanri.tools.modules.redis.service;

import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.protocol.param.AuthParam;
import com.sanri.tools.modules.protocol.param.ConnectParam;
import com.sanri.tools.modules.protocol.param.RedisConnectParam;
import com.sanri.tools.modules.redis.dto.RedisNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Client;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * redis 服务,需要保存 redis 客户端
 */
@Service
@Slf4j
public class RedisService {

}

package com.sanri.tools.modules.redis.service;

import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.protocol.param.AuthParam;
import com.sanri.tools.modules.protocol.param.ConnectParam;
import com.sanri.tools.modules.protocol.param.RedisConnectParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * redis 服务,需要保存 redis 客户端
 */
@Service
@Slf4j
public class RedisService {
    @Autowired
    private ConnectService connectService;

    public static final String module = "redis";

    // 保存的 jedis 客户端
    private Map<String, JedisWrap> jedisMap = new HashMap<String, JedisWrap>();

    /**
     * 获取数据库数量
     * @param connName
     * @return
     * @throws IOException
     */
    public int dbs(String connName) throws IOException {
        JedisWrap jedisWrap = jedisWrap(connName);
        Jedis jedis = jedisWrap.getJedis();
        List<String> databases = jedis.configGet("databases");
        int size = NumberUtils.toInt(databases.get(1));
        return size;
    }

    /**
     * 集合个数
     * @param name
     * @param index
     * @return
     * @throws IOException
     */
    public long dbSize(String connName,String index) throws IOException {
        JedisWrap jedisWrap = jedisWrap(connName);
        Jedis jedis = jedisWrap.getJedis();
        jedis.select(NumberUtils.toInt(index));
        return jedis.dbSize();
    }


    @PreDestroy
    public void destory(){
        Iterator<JedisWrap> iterator = jedisMap.values().iterator();
        while (iterator.hasNext()){
            JedisWrap wrapJedis = iterator.next();
            try {
                wrapJedis.getJedis().close();
            }catch (Exception e){}
        }
    }

    /**
     * 获取 jedis 实例
     * @param connName
     * @return
     * @throws IOException
     */
    private JedisWrap jedisWrap(String connName) throws IOException {
        JedisWrap jedisWrap = jedisMap.get(connName);
        if(jedisWrap == null){
            RedisConnectParam redisConnectParam = (RedisConnectParam) connectService.readConnParams(module, connName);
            AuthParam authParam = redisConnectParam.getAuthParam();
            ConnectParam connectParam = redisConnectParam.getConnectParam();

            String host = connectParam.getHost();
            int port = connectParam.getPort();
            int connectionTimeout = connectParam.getConnectionTimeout();
            int sessionTimeout = connectParam.getSessionTimeout();
            Jedis jedis = new Jedis(host, port, connectionTimeout, sessionTimeout);

            String password = authParam.getPassword();
            if(StringUtils.isNotBlank(password)){
                jedis.auth(password);
            }

            jedisMap.put(connName,new JedisWrap(jedis));
        }
        return jedisWrap;
    }
}

package com.sanri.tools.test;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;
import redis.clients.jedis.*;
import redis.clients.util.Slowlog;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedisMain {
    @Test
    public void slowlog(){
        Jedis jedis = new Jedis("192.168.0.134",6379);
        List<Slowlog> slowlogs = jedis.slowlogGet();
        System.out.println(slowlogs);
        jedis.close();
    }

    @Test
    public void testScan(){
        Jedis jedis = new Jedis("10.101.40.74",7000);
        jedis.auth("aiVK8ffFCV71L14h");

        Set<String> keys = jedis.keys("sanri*");
        System.out.println(keys);
    }

    @Test
    public void testClusterScan(){
        Set<HostAndPort> hostAndPorts = new HashSet<>();
        for (int i = 0; i < 3; i++) {
//            Jedis jedis = new Jedis("10.101.40.74",7000 + i);
//            jedis.auth("aiVK8ffFCV71L14h");
            hostAndPorts.add(new HostAndPort("10.101.40.74",7000+i));
        }
        for (int i = 0; i < 3; i++) {
            hostAndPorts.add(new HostAndPort("10.101.40.75",7000+i));
        }

        JedisCluster jedisCluster = new JedisCluster(hostAndPorts,2000, 5, 5, "aiVK8ffFCV71L14h", new GenericObjectPoolConfig());
//        ScanParams match = new ScanParams().count(1000).match("sanri*");
//        ScanResult<String> scan = jedisCluster.scan("0", match);
//        List<String> result = scan.getResult();
//        String stringCursor = scan.getStringCursor();
//        System.out.println("cursor:"+stringCursor);
//        System.out.println(result);

        jedisCluster.getClusterNodes().forEach((host,nodes) -> {
            Jedis resource = nodes.getResource();
            Set<String> keys = resource.keys("sanri*");
            System.out.println(keys);
        });
    }

    /**
     * 转移 redis 数据
     */
    @Test
    public void transRedisData() throws IOException {
        Jedis source = new Jedis("localhost",6379);
//        source.auth("liuyang@Redis#!^@199");
//        Jedis target = new Jedis("localhost",6379);
        HostAndPort hostAndPort = new HostAndPort("192.168.31.100",6379);
        HostAndPort hostAndPort1 = new HostAndPort("192.168.31.100",6380);
        HostAndPort hostAndPort2 = new HostAndPort("192.168.31.100",6381);
        HostAndPort hostAndPort3 = new HostAndPort("192.168.31.100",6389);
        HostAndPort hostAndPort4 = new HostAndPort("192.168.31.100",6390);
        HostAndPort hostAndPort5 = new HostAndPort("192.168.31.100",6391);
        final HashSet<HostAndPort> hostAndPorts = new HashSet<>();
        hostAndPorts.add(hostAndPort);
        hostAndPorts.add(hostAndPort1);
        hostAndPorts.add(hostAndPort2);
        hostAndPorts.add(hostAndPort3);
        hostAndPorts.add(hostAndPort4);
        hostAndPorts.add(hostAndPort5);
        JedisCluster target = new JedisCluster(hostAndPorts);

        int j = 0;
        for (int i = 0; i < 16; i++) {
            source.select(i);
//            target.select(i);
            System.out.println("移动数据库:"+i+" 数据量:"+source.dbSize());

            final Set<String> keys = source.keys("*");
            for (String key : keys) {
                final String type = source.type(key);
                System.out.println("移动 key:"+key+"类型:"+type+" 总共移动:"+(++j));

                if ("string".equals(type)){
                    target.set(key,source.get(key));
                }else if ("hash".equals(type)){
                    final Set<String> hkeys = source.hkeys(key);
                    for (String hkey : hkeys) {
                        target.hset(key,hkey,source.hget(key,hkey));
                    }
                }else if ("list".equals(type)){
                    final List<String> lrange = source.lrange(key, 0, source.llen(key));
                    for (String value : lrange) {
                        target.lpush(key,value);
                    }
                }else if ("set".equals(type)){
                    final Set<String> smembers = source.smembers(key);
                    for (String smember : smembers) {
                        target.sadd(key,smember);
                    }
                }else if ("zset".equals(type)){
                    final Set<String> zrange = source.zrange(key, 0, source.zcard(key));
                    for (String value : zrange) {
                        target.zadd(key,source.zscore(key,value),value);
                    }
                }
            }

            source.close();
        }

        target.close();
    }
}

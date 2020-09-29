package com.sanri.tools.modules.redis.service;

import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.classloader.ClassloaderService;
import com.sanri.tools.modules.redis.dtos.*;
import com.sanri.tools.modules.redis.dtos.params.*;
import com.sanri.tools.modules.serializer.service.Serializer;
import com.sanri.tools.modules.serializer.service.SerializerChoseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RedisClusterService {
    @Autowired
    private RedisService redisService;

    @Autowired
    private SerializerChoseService serializerChoseService;
    @Autowired
    private ClassloaderService classloaderService;

    /**
     * 集群的数据扫描
     * @param connParam
     * @param redisScanParam
     * @param serializerParam
     * @param hostIndex
     * @throws IOException
     * @return
     */
    public KeyScanResult scan(ConnParam connParam, RedisScanParam redisScanParam, SerializerParam serializerParam) throws IOException, ClassNotFoundException {
        JedisClient jedisClient = redisService.jedisClient(connParam);
        if (!jedisClient.isCluster){
            return redisService.scan(connParam,redisScanParam,serializerParam);
        }
        String cursor = redisScanParam.getCursor();
        String[] complexCursor = cursor.split("\\|");
        if (complexCursor.length != 2){
            throw  new ToolException("游标格式不正确 "+cursor);
        }
        redisScanParam.setCursor(complexCursor[0]);
        int hostIndex = NumberUtils.toInt(complexCursor[1]);

        JedisCluster jedisCluster = jedisCluster(jedisClient.jedis);
        List<Jedis> jedis = masterBrokers(jedisCluster);
        if (hostIndex >= jedis.size()){
            log.info("{} 节点数据扫描完毕",connParam);
            return null;
        }
        List<KeyScanResult.KeyResult> keyResults = new ArrayList<>();
        KeyScanResult keyScanResult = null;
        for (int i = hostIndex; i < jedis.size(); i++) {
            Jedis current = jedis.get(i);
            keyScanResult = redisService.nodeScan(current, redisScanParam, serializerParam);
            boolean finish = keyScanResult.isFinish();
            if (!finish){
                keyResults.addAll(keyScanResult.getKeys());
                redisScanParam.setLimit(redisScanParam.getLimit() - keyResults.size());
            }else{break;}
        }
        jedisCluster.close();
        keyScanResult.setKeys(keyResults);
        return keyScanResult;
    }

    /**
     * 客户端连接列表
     * @param connParam
     * @return
     * @throws IOException
     */
    public List<ClientConnection> clientList(ConnParam connParam) throws IOException {
        JedisClient jedisClient = redisService.jedisClient(connParam);
        if (!jedisClient.isCluster){
            return redisService.clientList(connParam);
        }

        List<ClientConnection> clientConnections = new ArrayList<>();
        JedisCluster jedisCluster = jedisCluster(jedisClient.jedis);
        List<Jedis> brokers = brokers(jedisCluster);
        for (Jedis broker : brokers) {
            ClientConnection clientConnection = redisService.nodeClientList(broker);
            clientConnections.add(clientConnection);
        }
        jedisCluster.close();
        return clientConnections;
    }

    /**
     * 连接的各节点内存占用情况
     * @param connParam
     * @return
     * @throws IOException
     */
    public List<MemoryUse> memoryUses(ConnParam connParam) throws IOException {
        JedisClient jedisClient = redisService.jedisClient(connParam);
        if (!jedisClient.isCluster){
            return redisService.memoryUses(connParam);
        }
        List<MemoryUse> memoryUses = new ArrayList<>();
        JedisCluster jedisCluster = jedisCluster(jedisClient.jedis);
        List<Jedis> brokers = brokers(jedisCluster);
        for (Jedis broker : brokers) {
            MemoryUse memoryUse = redisService.nodeMemoryUse(broker);
            memoryUses.add(memoryUse);
        }
        return memoryUses;
    }

    /**
     * 集群节点列表
     * @param connParam
     * @return
     * @throws IOException
     */
    public List<RedisNode> nodes(ConnParam connParam) throws IOException {
        JedisClient jedisClient = redisService.jedisClient(connParam);
        if (!jedisClient.isCluster){
            return redisService.nodes(connParam);
        }
        return clusterNodes(jedisClient.jedis);
    }

    /**
     * 删除部分 key
     * @param connParam
     * @param keys
     * @throws IOException
     */
    public void dropKeys(ConnParam connParam,String [] keys) throws IOException {
        JedisClient jedisClient = redisService.jedisClient(connParam);
        if (!jedisClient.isCluster){
            redisService.dropKeys(connParam,keys);
        }
        JedisCluster jedisCluster = jedisCluster(jedisClient.jedis);
        jedisCluster.del(keys);
    }

    /**
     * 集群子键扫描
     * @param connParam
     * @param key
     * @param redisScanParam
     * @param serializerParam
     * @return
     * @throws IOException
     */
    public SubKeyScanResult subKeyScan(ConnParam connParam,String key,RedisScanParam redisScanParam,SerializerParam serializerParam) throws IOException, ClassNotFoundException {
        JedisClient jedisClient = redisService.jedisClient(connParam);
        if (!jedisClient.isCluster){
            redisService.subKeyScan(connParam,key,redisScanParam,serializerParam);
        }
        JedisCluster jedisCluster = jedisCluster(jedisClient.jedis);
        return clientSubKeyScan(jedisCluster,key,redisScanParam,serializerParam);
    }

    /**
     * 数据查询,对于某些子 key
     * @param connParam
     * @param subKeyParam
     * @param param
     * @return
     */
    public Object data(ConnParam connParam, SubKeyParam subKeyParam, RangeParam rangeParam, RedisScanParam redisScanParam, SerializerParam serializerParam) throws IOException, ClassNotFoundException {
        JedisClient jedisClient = redisService.jedisClient(connParam);
        if (!jedisClient.isCluster){
            return redisService.data(connParam,subKeyParam,rangeParam,redisScanParam,serializerParam);
        }

        JedisCluster client = jedisCluster(jedisClient.jedis);

        Serializer keySerializer = serializerChoseService.choseSerializer(serializerParam.getKeySerializer());
        Serializer valueSerializer = serializerChoseService.choseSerializer(serializerParam.getValue());
        Serializer hashKeySerializer = serializerChoseService.choseSerializer(serializerParam.getHashKey());
        Serializer hashValueSerializer = serializerChoseService.choseSerializer(serializerParam.getHashValue());
        ClassLoader classloader = classloaderService.getClassloader(serializerParam.getClassloaderName());

        byte[] keyBytes = keySerializer.serialize(subKeyParam.getKey());

        String type = client.type(keyBytes);
        RedisService.RedisType redisType = RedisService.RedisType.parse(type);
        switch (redisType){
            case string:
                byte[] value = client.get(keyBytes);
                return valueSerializer.deserialize(value,classloader);
            case List:
                Long start = rangeParam.getStart();
                Long stop = rangeParam.getStop();
                List<byte[]> lrange = client.lrange(keyBytes, start, stop);
                List<Object> values = new ArrayList<>();
                for (byte[] bytes : lrange) {
                    Object deserialize = valueSerializer.deserialize(bytes, classloader);
                    values.add(deserialize);
                }
                return values;
            case Hash:
                boolean all = subKeyParam.isAll();
                Map<byte[], byte[]> map = new HashMap<>();
                Map<Object, Object> mapValues = new HashMap<>();
                if (all){
                    map = client.hgetAll(keyBytes);
                }else{
                    String[] subKeys = subKeyParam.getSubKeys();
                    for (String subKey : subKeys) {
                        byte[] subKeyBytes = hashKeySerializer.serialize(subKey);
                        byte[] valueBytes = client.hget(keyBytes, subKeyBytes);
                        map.put(subKeyBytes,valueBytes);
                    }
                }
                Iterator<Map.Entry<byte[], byte[]>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<byte[], byte[]> next = iterator.next();
                    byte[] subKey = next.getKey();
                    byte[] valueBytes = next.getValue();

                    Object hashKey = hashKeySerializer.deserialize(subKey,classloader);
                    Object hashValue = hashValueSerializer.deserialize(valueBytes, classloader);
                    mapValues.put(hashKey,hashValue);
                }
                return mapValues;
            case Set:
                if (subKeyParam.isAll()) {
                    Set<byte[]> smembers = jedisClient.jedis.smembers(keyBytes);
                    Set<Object> setValues = new HashSet<>();
                    for (byte[] smember : smembers) {
                        Object deserialize = valueSerializer.deserialize(smember, classloader);
                        setValues.add(deserialize);
                    }
                    return setValues;
                }else {
                    String cursor = redisScanParam.getCursor();
                    int limit = redisScanParam.getLimit();
                    ScanParams scanParams = new ScanParams();
                    scanParams.match(redisScanParam.getPattern()).count(limit);
                    List<byte[]> smembers = new ArrayList<>();
                    do {
                        ScanResult<byte[]> scan = client.sscan(keyBytes, cursor.getBytes(), scanParams);
                        List<byte[]> subScan = scan.getResult();
                        for (byte[] setValue : subScan) {
                            smembers.add(setValue);
                        }

                        cursor = scan.getStringCursor();
                        scanParams.count(limit - subScan.size());
                    } while (smembers.size() < limit && NumberUtils.toLong(cursor) != 0L);

                    // 对扫描到的数据进行使用序列化解析
                    List<Object> smemberObjects = new ArrayList<>();
                    for (byte[] smember : smembers) {
                        smemberObjects.add(valueSerializer.deserialize(smember, classloader));
                    }
                    SubKeyScanResult<Object> subKeyScanResult = new SubKeyScanResult(smemberObjects, cursor);
                    if ("0".equals(cursor)) {
                        subKeyScanResult.setFinish(true);
                    }
                    return subKeyScanResult;
                }
            case ZSet:
                if (rangeParam.getStart() != null && rangeParam.getStop() != null){
                    Set<Tuple> tuples = client.zrangeWithScores(keyBytes, rangeParam.getStart(), rangeParam.getStop());
                    List<ZSetTuple> zSetTuples = redisService.mapperZsetTuple(valueSerializer,classloader,tuples);
                    return zSetTuples;
                }
                if (rangeParam.getMin() != null && rangeParam.getMax() != null){
                    Set<Tuple> tuples = client.zrangeByScoreWithScores(keyBytes, rangeParam.getMin(), rangeParam.getMax());
                    List<ZSetTuple> zSetTuples = redisService.mapperZsetTuple(valueSerializer, classloader, tuples);
                    return zSetTuples;
                }

                String cursor = redisScanParam.getCursor();
                int limit = redisScanParam.getLimit();
                ScanParams scanParams = new ScanParams();
                scanParams.match(redisScanParam.getPattern()).count(limit);

                Set<Tuple> tuples = new HashSet<>();
                do {
                    ScanResult<Tuple> zscan = client.zscan(keyBytes, cursor.getBytes(), scanParams);
                    List<Tuple> subScan = zscan.getResult();
                    tuples.addAll(subScan);

                    cursor = zscan.getStringCursor();
                    scanParams.count(limit - subScan.size());
                }while (tuples.size() < limit && NumberUtils.toLong(cursor) != 0L);

                List<ZSetTuple> zSetTuples = redisService.mapperZsetTuple(valueSerializer, classloader, tuples);
                SubKeyScanResult<ZSetTuple> subKeyScanResult = new SubKeyScanResult<ZSetTuple>(zSetTuples, cursor);
                if ("0".equals(cursor)){
                    subKeyScanResult.setFinish(true);
                }
                return subKeyScanResult;

        }

        return null;
    }

    /**
     * 子键扫描 | 这个代码其实和 RedisService 中同名方法一样 , 只是类换了下 Jedis 换成了 JedisCluster
     * 用父级方法不能使用序列化工具 JedisCommands
     * @param client
     * @param key
     * @param redisScanParam
     * @param serializerParam
     * @return
     */
    private SubKeyScanResult clientSubKeyScan(JedisCluster client , String key,RedisScanParam redisScanParam,SerializerParam serializerParam) throws IOException, ClassNotFoundException {
        ClassLoader classloader = classloaderService.getClassloader(serializerParam.getClassloaderName());
        Serializer keySerializer = serializerChoseService.choseSerializer(serializerParam.getKeySerializer());
        Serializer hashKeySerializer = serializerChoseService.choseSerializer(serializerParam.getHashKey());

        byte[] keyBytes = keySerializer.serialize(key);

        String type = client.type(keyBytes);
        RedisService.RedisType redisType = RedisService.RedisType.parse(type);
        if (redisType == null || (redisType != RedisService.RedisType.Hash && redisType != RedisService.RedisType.Set && redisType != RedisService.RedisType.ZSet)){
            throw new ToolException("不支持的 redis 类型["+redisType+"],在子键["+key+"]扫描");
        }

        int limit = redisScanParam.getLimit();
        ScanParams scanParams = new ScanParams();
        scanParams.match(redisScanParam.getPattern()).count(limit);
        String cursor = redisScanParam.getCursor();
        List<byte[]> keys = new ArrayList<>();
        switch (redisType){
            case Hash:
                do {
                    ScanResult<Map.Entry<byte[], byte[]>> hscan = client.hscan(keyBytes, cursor.getBytes(), scanParams);
                    List<Map.Entry<byte[], byte[]>> hscanResult = hscan.getResult();
                    for (Map.Entry<byte[], byte[]> stringStringEntry : hscanResult) {
                        byte [] hashKey = stringStringEntry.getKey();
                        keys.add(hashKey);
                    }

                    cursor = hscan.getStringCursor();
                    scanParams.count(limit - hscanResult.size());
                }while (keys.size() < limit && NumberUtils.toLong(cursor) != 0L);
                break;
        }

        List<String> stringKeys = new ArrayList<>();
        for (byte[] bytes : keys) {
            Object deserialize = hashKeySerializer.deserialize(bytes, classloader);
            stringKeys.add(Objects.toString(deserialize));
        }

        SubKeyScanResult subKeyScanResult = new SubKeyScanResult(stringKeys, cursor);
        if ("0".equals(cursor)){
            subKeyScanResult.setFinish(true);
        }

        return subKeyScanResult;
    }


//    /**
//     * 范围 key 列表查询
//     * @param connParam
//     * @param key
//     * @param rangeParam
//     * @param serializerParam
//     * @return
//     * @throws IOException
//     */
//    public List<String> rangeKeys(ConnParam connParam, String key, RangeParam rangeParam, SerializerParam serializerParam) throws IOException{
//        JedisClient jedisClient = redisService.jedisClient(connParam);
//        JedisCluster jedisCluster = jedisCluster(jedisClient.jedis);
//        return redisService.clientRangeKeys(jedisCluster,key,rangeParam,serializerParam);
//    }

    /**
     * 获取集群客户端
     * @param client
     * @return
     * @throws IOException
     */
    private JedisCluster jedisCluster(Jedis client) throws IOException {
        List<RedisNode> redisNodes = clusterNodes(client);
        Set<HostAndPort> hostAndPorts = redisNodes.stream().map(RedisNode::getHostAndPort).collect(Collectors.toSet());
        return new JedisCluster(hostAndPorts);
    }

    /**
     * 获取集群所有节点信息
     * @param jedisCluster
     * @return
     */
    private List<Jedis> brokers(JedisCluster jedisCluster){
        Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
        List<Jedis> brokers = clusterNodes.values().stream().map(JedisPool::getResource).collect(Collectors.toList());
        return brokers;
    }

    /**
     * 获取集群所有的 master 节点
     * @param jedisCluster
     * @return
     */
    private List<Jedis> masterBrokers(JedisCluster jedisCluster){
        List<Jedis> brokers = brokers(jedisCluster);

        // 去掉非 master 节点
        Iterator<Jedis> jedisIterator = brokers.iterator();
        while (jedisIterator.hasNext()){
            Jedis next = jedisIterator.next();
            String role = redisService.jedisRole(next);
            if (!"master".equals(role)){
                jedisIterator.remove();
            }
        }

        // 对 nodes 进行排序,保证搜索顺序
        Collections.sort(brokers,(a,b)-> (a.getClient().getHost()+a.getClient().getPort()).compareTo(b.getClient().getHost()+b.getClient().getPort()));

        return brokers;
    }

    /**
     * 获取集群所有节点
     * @param jedis
     * @return
     */
    private List<RedisNode> clusterNodes(Jedis jedis) {
        Client client = jedis.getClient();
        client.clusterNodes();
        String bulkReply = client.getBulkReply();
        List<String []> nodeCommandLines = CommandReply.spaceCommandReply.parser(bulkReply);
        List<RedisNode> redisNodes = nodeCommandLines.stream().map(line -> {
            RedisNode redisNode = new RedisNode();
            redisNode.setId(line[0]);
            String[] split1 = line[1].split("@");
            redisNode.setHostAndPort(HostAndPort.parseString(split1[0]));
            String flags = line[2];
            redisNode.setRole(flags.replace("myself,", ""));
            redisNode.setMaster(line[3]);
            if ("master".equals(redisNode.getRole())) {
                String slots = line[8];
                if(slots.contains("-")){
                    String[] split = StringUtils.split(slots, '-');
                    int start = NumberUtils.toInt(split[0]);int end = NumberUtils.toInt(split[1]);
                    redisNode.setSlotStart(start);redisNode.setSlotEnd(end);
                }else{
                    int around = NumberUtils.toInt(slots);
                    redisNode.setSlotStart(around);redisNode.setSlotEnd(around);
                }
            }
            return redisNode;
        }).collect(Collectors.toList());

        return redisNodes;
    }
}

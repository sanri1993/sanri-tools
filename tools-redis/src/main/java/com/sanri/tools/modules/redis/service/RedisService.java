package com.sanri.tools.modules.redis.service;

import com.sanri.tools.modules.core.service.classloader.ClassloaderService;
import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.core.dtos.PluginDto;
import com.sanri.tools.modules.core.service.plugin.PluginManager;
import com.sanri.tools.modules.core.dtos.param.AuthParam;
import com.sanri.tools.modules.core.dtos.param.ConnectParam;
import com.sanri.tools.modules.core.dtos.param.RedisConnectParam;
import com.sanri.tools.modules.redis.dtos.*;
import com.sanri.tools.modules.serializer.service.Serializer;
import com.sanri.tools.modules.serializer.service.SerializerChoseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * redis 服务,需要保存 redis 客户端
 */
@Service
@Slf4j
public class RedisService {
    // 保存的 jedis 客户端
    private Map<String, Jedis> jedisMap = new ConcurrentHashMap<>();
    @Autowired
    private ConnectService connectService;
    @Autowired
    private PluginManager pluginManager;

    public static final String module = "redis";

    @Autowired
    private SerializerChoseService serializerChoseService;
    @Autowired
    private ClassloaderService classloaderService;

    /**
     * 当前 redis 模式
     * @return
     */
    public String mode(String connName) throws IOException {
        Jedis jedis = jedis(connName);
        boolean cluster = isCluster(jedis);
        if(cluster){
            return "cluster";
        }
        List<RedisNode> redisNodes = masterSlaveNodes(jedis);
        if(redisNodes.size() == 1){
            return "local";
        }
        return "master-slave";
    }

    /**
     * 扫描 redis 的 key
     * @param redisScanParam
     * @return
     * @throws IOException
     */
    public List<RedisKeyResult> scan(RedisScanParam redisScanParam) throws IOException, ClassNotFoundException {
        String connName = redisScanParam.getConnName();
        Jedis jedis = jedis(connName);
        boolean cluster = isCluster(jedis);

        int limit = redisScanParam.getLimit();
        String pattern = redisScanParam.getPattern();
        String cursor = redisScanParam.getCursor();

        JedisCommands client = jedis;

        // 开始搜索每个节点上的 key 列表
        Set<String> keys = new HashSet<>();
        String keySerializer = redisScanParam.getKeySerializer();
        Serializer serializer = serializerChoseService.choseSerializer(keySerializer);
        if(!cluster){
            int index = redisScanParam.getIndex();
            jedis.select(index);
            keys.addAll(nodeScan(jedis, pattern, limit,cursor,serializer));
        }else{
            JedisCluster jedisCluster = jedisCluster(jedis);
            client = jedisCluster;
            // ip:port => JedisPool
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            Iterator<JedisPool> iterator = clusterNodes.values().iterator();
            while (iterator.hasNext()){
                JedisPool jedisPool = iterator.next();
                Jedis currentJedis = jedisPool.getResource();
                // 判断当前节点是否为 master ,只从 master 取数据; 目前还是从所有节点中找数据
                List<String> currentKeys = nodeScan(currentJedis, pattern, limit,cursor,serializer);
                keys.addAll(currentKeys);
                if(keys.size() >= limit){break;}
            }
        }

        // 对找到的 key 进行包装
        List<RedisKeyResult> redisKeyResults = new ArrayList<>();
        for (String key : keys) {
            String type = client.type(key);
            Long ttl = client.ttl(key);
            Long pttl = client.ttl(key);
            RedisKeyResult redisKeyResult = new RedisKeyResult(key, type, ttl, pttl);
            long length = keyLength(client, key);
            redisKeyResult.setLength(length);

            redisKeyResults.add(redisKeyResult);
        }

        closeCluster(cluster, client);

        return redisKeyResults;
    }

    /**
     * 获取 key 长度
     * @param client
     * @param key
     * @return
     */
    private long keyLength(JedisCommands client, String key) {
        String type = client.type(key);
        RedisType redisType = RedisType.parse(type);
        switch (redisType){
            case string:
                return client.strlen(key);
            case Set:
            case ZSet:
            case List:
                return client.llen(key);
            case Hash:
                return client.hlen(key);
        }

        return 0;
    }

    private void closeCluster(boolean cluster, JedisCommands client) throws IOException {
        if(cluster && client != null){
            // 如果是集群模式,需要关闭集群客户端
            JedisCluster jedisCluster = (JedisCluster) client;
            jedisCluster.close();
        }
    }

    /**
     * 获取 redis 节点列表
     * @param connName
     * @return
     * @throws IOException
     */
    public List<RedisNode> nodes(String connName) throws IOException {
        Jedis jedis = jedis(connName);
        boolean cluster = isCluster(jedis);
        if(cluster){
            return clusterNodes(jedis);
        }
        return masterSlaveNodes(jedis);
    }

    /**
     * redis 的数据类型
     */
    enum RedisType{
        string("string"),Set("set"),ZSet("zset"),Hash("hash"),List("list");
        private String value;

        RedisType(String value) {
            this.value = value;
        }

        public static RedisType parse(String type){
            RedisType[] values = RedisType.values();
            for (RedisType value : values) {
                if(value.value.equals(type)){
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 查询 list 数据键的长度
     * @param redisCommandParam
     * @return
     * @throws IOException
     */
    public Long queryKeyLength(RedisCommandParam redisCommandParam) throws IOException{
        String connName = redisCommandParam.getConnName();
        String key = redisCommandParam.getKey();

        Jedis jedis = jedis(connName);
        JedisCommands client = jedis;
        boolean cluster = isCluster(jedis);
        if(cluster){
            client = jedisCluster(jedis);
        }else{
            int index = redisCommandParam.getIndex();
            jedis.select(index);
        }

        long length = keyLength(client, key);

        closeCluster(cluster,client);

        return length;
    }

    /**
     * 查出指定数量的 hashKey
     * @param hashKeyCommandParam
     * @return
     */
    public List<String> hashKeyScan(HashScanParam hashScanParam) throws IOException, ClassNotFoundException {
        String connName = hashScanParam.getConnName();
        String hashKeySerizlizer = hashScanParam.getHashKeySerizlizer();
        Serializer serializer = serializerChoseService.choseSerializer(hashKeySerizlizer);
        String key = hashScanParam.getKey();
        String keySerializer = hashScanParam.getKeySerializer();
        Serializer keySerializerImpl = serializerChoseService.choseSerializer(keySerializer);
        byte[] keyBytes = keySerializerImpl.serialize(key);

        Jedis jedis = jedis(connName);
        JedisCommands client = jedis;
        boolean cluster = isCluster(jedis);
        if(cluster){
            client = jedisCluster(jedis);
        }else{
            int index = hashScanParam.getIndex();
            jedis.select(index);
        }

        // 构建搜索参数
        int limit = hashScanParam.getLimit();
        String pattern = hashScanParam.getPattern();
        ScanParams scanParams = new ScanParams();
        scanParams.count(limit);
        if(StringUtils.isNotBlank(pattern)){
            scanParams.match(pattern);
        }
        String cursor = "0";
        List<String> hashKeyResult = new ArrayList<>();
        do {
            ScanResult<Map.Entry<byte[], byte[]>> scanResult = jedis.hscan(keyBytes,cursor.getBytes(), scanParams);
            List<Map.Entry<byte[],byte[]>> result = scanResult.getResult();
            for (Map.Entry<byte[], byte[]> entry : result) {
                hashKeyResult.add(Objects.toString(serializer.deserialize(entry.getKey(), ClassLoader.getSystemClassLoader())));
            }
            cursor = scanResult.getStringCursor();
        }while (hashKeyResult.size() < limit && NumberUtils.toLong(cursor) != 0L);

        closeCluster(cluster,client);

        return hashKeyResult;
    }

    /**
     * 加载 redis 中的数据
     * @param dataQueryParam
     * @return
     */
    public Object loadData(RedisDataQueryParam dataQueryParam) throws IOException, ClassNotFoundException {
        String connName = dataQueryParam.getConnName();
        String key = dataQueryParam.getKey();
        String keySerializerChose = dataQueryParam.getKeySerializer();
        ExtraQueryParam extraQueryParam = dataQueryParam.getExtraQueryParam();
        SerializerChose serializerChose = dataQueryParam.getSerializerChose();
        String hashKeySerializerChose = serializerChose.getHashKey();
        Serializer hashKeySerializer = serializerChoseService.choseSerializer(hashKeySerializerChose);
        Serializer keySerializer = serializerChoseService.choseSerializer(keySerializerChose);
        byte[] keyBytes = keySerializer.serialize(key);

        // 接收数据,准备反序列化
        byte [] valueBytes = null;
        Map<byte[],byte[]> hashValueBytes = new HashMap<>();
        List<byte[]> listValueBytes = null;

        // 开始查询数据
        Jedis jedis = jedis(connName);
        boolean cluster = isCluster(jedis);
        RedisType redisType = null;
        if(cluster){
            JedisCluster jedisCluster = jedisCluster(jedis);
            String type = jedisCluster.type(key);
            redisType = RedisType.parse(type);

            switch (redisType){
                case string:
                    valueBytes = jedisCluster.get(keyBytes);
                    break;
                case Hash:
                    if (extraQueryParam != null) {
                        String hashKey = extraQueryParam.getHashKey();
                        byte[] cursor = "0".getBytes();
                        byte[] serialize = hashKeySerializer.serialize(hashKey);
                        ScanParams scanParams = new ScanParams().match(serialize).count(100);
                        do {
                            ScanResult<Map.Entry<byte[], byte[]>> entryScanResult = jedisCluster.hscan(keyBytes, cursor, scanParams);
                            cursor = entryScanResult.getCursorAsBytes();
                            List<Map.Entry<byte[], byte[]>> result = entryScanResult.getResult();
                            for (Map.Entry<byte[], byte[]> entry : result) {
                                hashValueBytes.put(entry.getKey(), entry.getValue());
                            }
                        } while (NumberUtils.toInt(new String(cursor)) != 0);

                        break;
                    }
                    Map<byte[], byte[]> map = jedisCluster.hgetAll(keyBytes);
                    Iterator<Map.Entry<byte[], byte[]>> iterator = map.entrySet().iterator();
                    while (iterator.hasNext()){
                        Map.Entry<byte[], byte[]> entry = iterator.next();
                        hashValueBytes.put(entry.getKey(), entry.getValue());
                    }

                    break;
                case List:
                    if(extraQueryParam != null) {
                        Long begin = extraQueryParam.getBegin();
                        if (begin == null) {
                            begin = 0L;
                        }
                        Long end = extraQueryParam.getEnd();
                        if (end == null) {
                            end = jedisCluster.llen(keyBytes);
                        }

                        listValueBytes = jedisCluster.lrange(keyBytes, begin, end);
                        break;
                    }
                    listValueBytes = jedisCluster.lrange(keyBytes, 0, jedisCluster.llen(keyBytes));
                    break;
            }

            jedisCluster.close();
        }else{
            int index = dataQueryParam.getIndex();
            jedis.select(index);

            String type = jedis.type(key);
            redisType = RedisType.parse(type);
            switch (redisType){
                case string:
                    valueBytes = jedis.get(keyBytes);
                    break;
                case Hash:
                    String hashKey = extraQueryParam.getHashKey();
                    byte[] cursor = "0".getBytes();
                    byte[] serialize = hashKeySerializer.serialize(hashKey);
                    ScanParams scanParams = new ScanParams().match(serialize).count(100);
                    do {
                        ScanResult<Map.Entry<byte[], byte[]>> entryScanResult = jedis.hscan(keyBytes, cursor, scanParams);
                        cursor = entryScanResult.getCursorAsBytes();
                        List<Map.Entry<byte[], byte[]>> result = entryScanResult.getResult();
                        for (Map.Entry<byte[], byte[]> entry : result) {
                            hashValueBytes.put(entry.getKey(),entry.getValue());
                        }
                    }while (NumberUtils.toInt(new String(cursor)) != 0);
                    break;
                case List:
                    Long begin = extraQueryParam.getBegin();if(begin == null){begin = 0L;}
                    Long end = extraQueryParam.getEnd();if(end == null ){end = jedis.llen(keyBytes);}

                    listValueBytes = jedis.lrange(keyBytes,begin,end);
                    break;
            }
        }

        if(valueBytes == null && hashValueBytes == null && listValueBytes == null){
            log.warn("key [{}] , extra [{}]不存在 ",key,dataQueryParam.getExtraQueryParam());
            return null;
        }

        // 开始反序列化数据
        String valueSerializerChose = serializerChose.getValue();
        String hashValueSerializerChose = serializerChose.getHashValue();
        Serializer valueSerializer = serializerChoseService.choseSerializer(valueSerializerChose);
        Serializer hashValueSerializer = serializerChoseService.choseSerializer(hashValueSerializerChose);
        String classloaderName = dataQueryParam.getClassloaderName();
        ClassLoader classloader = classloaderService.getClassloader(classloaderName);
        if(classloader == null){
            classloader = ClassLoader.getSystemClassLoader();
        }

        Object object = null;
        switch (redisType){
            case string:
                if(keyBytes == null){
                    log.warn("type:string ,key [{}] 不存在 ",key);
                    return null;
                }
                object = valueSerializer.deserialize(valueBytes,classloader);
                return object;
            case Hash:
                if(hashValueBytes == null || hashValueBytes.size() == 0){
                    log.warn("type:hash ,key [{}] 不存在或没有数据 ",key);
                    return null;
                }
                Map<String,Object> hashObjects = new HashMap<>(hashValueBytes.size());
                Iterator<Map.Entry<byte[], byte[]>> iterator = hashValueBytes.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<byte[], byte[]> next = iterator.next();
                    byte[] hashKey = next.getKey();
                    byte[] hashValue = next.getValue();
                    object = hashValueSerializer.deserialize( hashValue, classloader);
                    Object keyObject = hashKeySerializer.deserialize(hashKey,classloader);
                    hashObjects.put(Objects.toString(keyObject), object);
                }
                return hashObjects;
            case List:
                if (listValueBytes == null || listValueBytes.size() == 0){
                    log.warn("type:list ,key [{}] 不存在或没有数据 ",key);
                    return null;
                }
                List<Object> listObjects = new ArrayList<>(listValueBytes.size());
                for (byte[] listValueByte : listValueBytes) {
                    object = hashValueSerializer.deserialize( listValueByte, classloader);
                    listObjects.add(object);
                }
                return listObjects;
        }

        return null;
    }

    /**
     * 单个节点的 key 扫描
     * @param jedis
     * @param pattern
     * @param limit
     * @param keySerializer
     * @return
     */
    private List<String> nodeScan(Jedis jedis, String pattern, int limit,String cursor, Serializer serializer) throws IOException, ClassNotFoundException {
        ScanParams scanParams = new ScanParams();
        scanParams.count(limit);
        if(StringUtils.isNotBlank(pattern)) {
            scanParams.match(pattern);
        }
        //如果搜索结果为空,则继续搜索,直到有值或搜索到末尾
        List<String> keyAllReuslts = new ArrayList<>();
        do {
            ScanResult scanResult = jedis.scan(cursor.getBytes(), scanParams);
            List<byte[]> result = scanResult.getResult();
            for (byte[] bytes : result) {
                keyAllReuslts.add(Objects.toString(serializer.deserialize(bytes, ClassLoader.getSystemClassLoader())));
            }
            cursor = scanResult.getStringCursor();
        }while (keyAllReuslts.size() < limit && NumberUtils.toLong(cursor) != 0L);

        return keyAllReuslts;
    }

    /**
     * 获取主从模式下所有节点
     * @param jedis
     * @return
     */
    private List<RedisNode> masterSlaveNodes(Jedis jedis){
        List<RedisNode> redisNodes = new ArrayList<>();

        //如果不是集群模式,看是否为主从模式,获取主从结构的所有节点
        String replication = jedis.info("Replication");
        Map<String, String> properties = ColonCommandReply.colonCommandReply.parserKeyValue(replication);
        String connected_slaves = properties.get("connected_slaves");
        if(StringUtils.isNotBlank(connected_slaves)) {
            int slaves = NumberUtils.toInt(connected_slaves);
            if(slaves == 0){
                // 单机模式
                RedisNode redisNode = new RedisNode();
                String host = jedis.getClient().getHost();
                int port = jedis.getClient().getPort();
                redisNode.setId(host+":"+port);
                redisNode.setRole("master");
                redisNode.setHostAndPort(HostAndPort.parseString(redisNode.getId()));
                return Collections.singletonList(redisNode);
            }

            // 否则就是主从模式,级联获取所有节点
            Client client = jedis.getClient();
            findSlaves(HostAndPort.parseString(client.getHost()+":"+client.getPort()),redisNodes,null);

        }
        return redisNodes;
    }
    private void findSlaves(HostAndPort hostAndPort,List<RedisNode> redisNodes,String masterId) {
        // 先添加父节点
        Jedis jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort());
        String replication = jedis.info("Replication");jedis.disconnect();
        Map<String, String> properties = ColonCommandReply.colonCommandReply.parserKeyValue(replication);
        RedisNode redisNode = new RedisNode();
        redisNode.setId(hostAndPort.toString());
        redisNode.setRole(properties.get("role"));
        redisNode.setHostAndPort(hostAndPort);
        redisNode.setMaster(masterId);
        redisNodes.add(redisNode);

        // 添加子节点
        Iterator<Map.Entry<String, String>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> next = iterator.next();
            String key = next.getKey();
            if(key.startsWith("slave")){
                String value = next.getValue();
                String[] split = StringUtils.split(value, ',');
                String host = split[0].split("=")[1];int port = NumberUtils.toInt(split[1].split("=")[1]);
                findSlaves(HostAndPort.parseString(host+":"+port),redisNodes,hostAndPort.toString());
            }
        }
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
            redisNode.setHostAndPort(HostAndPort.parseString(line[1]));
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

    /**
     * 判断是否为集群模式
     * @param jedis
     * @return
     */
    private boolean isCluster(Jedis jedis) {
        String info = jedis.info("Cluster");
        Map<String, String> properties = ColonCommandReply.colonCommandReply.parserKeyValue(info);
        String cluster_enabled = properties.get("cluster_enabled");
        if("1".equals(cluster_enabled)){
            return true;
        }
        return false;
    }


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
     * 获取一个客户端
     * @return
     */
    Jedis jedis(String connName) throws IOException {
        Jedis jedis = jedisMap.get(connName);
        if(jedis == null){
            // 获取连接参数
            RedisConnectParam redisConnectParam = (RedisConnectParam) connectService.readConnParams(module,connName);
            ConnectParam connectParam = redisConnectParam.getConnectParam();
            jedis = new Jedis(connectParam.getHost(), connectParam.getPort(), connectParam.getConnectionTimeout(), connectParam.getSessionTimeout());
            AuthParam authParam = redisConnectParam.getAuthParam();
            if(authParam != null) {
                String password = authParam.getPassword();
                if (StringUtils.isNotBlank(password)) {
                    jedis.auth(password);
                }
            }
            jedisMap.put(connName,jedis);
        }

        return jedis;
    }

    @PostConstruct
    public void register(){
        pluginManager.register(PluginDto.builder().module(module).name("main").author("sanri").envs("default").build());
    }

    @PreDestroy
    public void destory(){
        log.info("清除 {} 客户端列表:{}",module,jedisMap.keySet());
        Iterator<Jedis> iterator = jedisMap.values().iterator();
        while (iterator.hasNext()){
            Jedis next = iterator.next();
            if(next != null){
                try {
                    next.close();
                } catch (Exception e) {}
            }
        }
    }
}

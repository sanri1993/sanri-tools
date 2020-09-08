package com.sanri.tools.modules.redis.service;

import com.sanri.tools.modules.core.service.classloader.ClassloaderService;
import com.sanri.tools.modules.core.service.data.regex.Node;
import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.core.dtos.PluginDto;
import com.sanri.tools.modules.core.service.plugin.PluginManager;
import com.sanri.tools.modules.core.dtos.param.AuthParam;
import com.sanri.tools.modules.core.dtos.param.ConnectParam;
import com.sanri.tools.modules.core.dtos.param.RedisConnectParam;
import com.sanri.tools.modules.redis.dtos.*;
import com.sanri.tools.modules.serializer.service.Serializer;
import com.sanri.tools.modules.serializer.service.SerializerChoseService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;
import redis.clients.util.JedisClusterCRC16;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.Field;
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
     * 各节点内存使用情况
     * @param connName
     * @return
     * @throws IOException
     */
    public List<MemoryUse> memoryUse(String connName) throws IOException {
        Jedis jedis = jedis(connName);
        boolean cluster = isCluster(jedis);
        List<MemoryUse> memoryUses = new ArrayList<>();
        if (cluster){
            JedisCluster jedisCluster = jedisCluster(jedis);
            // ip:port => JedisPool
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            Iterator<JedisPool> iterator = clusterNodes.values().iterator();
            while (iterator.hasNext()){
                JedisPool jedisPool = iterator.next();
                Jedis client = jedisPool.getResource();
                memoryUses.add(nodeMemoryUse(client));
            }

            closeCluster(cluster,jedisCluster);

            return memoryUses;
        }
        return Collections.singletonList(nodeMemoryUse(jedis));
    }

    private MemoryUse nodeMemoryUse(Jedis jedis){
        String host = jedis.getClient().getHost();
        int port = jedis.getClient().getPort();
        HostAndPort target = new HostAndPort(host, port);
        MemoryUse memoryUse = new MemoryUse(target);
        String info = jedis.info("Memory");
        List<String[]> parser = CommandReply.colonCommandReply.parser(info);

        for (String[] line : parser) {
            if (line.length == 2){
                if ("used_memory_rss".equals(line[0])){
                    memoryUse.setRss(NumberUtils.toLong(line[1]));
                }else if ("used_memory_lua".equals(line[0])){
                    memoryUse.setLua(NumberUtils.toLong(line[1]));
                }else if ("maxmemory".equals(line[0])){
                    memoryUse.setMax(NumberUtils.toLong(line[1]));
                }else if ("total_system_memory".equals(line[0])){
                    memoryUse.setSystem(NumberUtils.toLong(line[1]));
                }else if ("maxmemory_policy".equals(line[0])){
                    memoryUse.setPolicy(line[1]);
                }
            }
        }
        return memoryUse;
    }

    /**
     * 列出所有的客户端和客户端占用连接数
     * @param connName
     * @throws IOException
     * @return
     */
    public List<ClientConnection> clientList(String connName) throws IOException {
        Jedis jedis = jedis(connName);
        boolean cluster = isCluster(jedis);
        if (cluster){
            List<ClientConnection> allClientConnections = new ArrayList<>();

            JedisCluster jedisCluster = jedisCluster(jedis);
            // ip:port => JedisPool
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            Iterator<JedisPool> iterator = clusterNodes.values().iterator();
            while (iterator.hasNext()){
                JedisPool jedisPool = iterator.next();
                Jedis client = jedisPool.getResource();
                List<ClientConnection> clientConnections = nodeClientList(client);
                allClientConnections.addAll(clientConnections);
            }

            closeCluster(cluster,jedisCluster);

            return allClientConnections;
        }
        return nodeClientList(jedis);
    }

    /**
     * 单个节点的客户端连接列表
     * @param jedis
     * @return
     */
    public List<ClientConnection> nodeClientList(Jedis jedis) {
        String clientList = jedis.clientList();
        String host = jedis.getClient().getHost();
        int port = jedis.getClient().getPort();
        HostAndPort target = new HostAndPort(host, port);

        List<Map<String, String>> mapList = CommandReply.spaceCommandReply.parserWithHeader(clientList, "id", "addr", "fd", "name", "age", "idle", "flags", "db", "sub", "psub", "multi", "qbuf", "qbuf-free", "obl", "oll", "omem", "events", "cmd");
        Class<ClientConnection> clientConnectionClass = ClientConnection.class;
        List<ClientConnection> clientConnections  = new ArrayList<>();
        for (Map<String, String> props : mapList) {
            ClientConnection clientConnection = new ClientConnection(target);
            clientConnections.add(clientConnection);
            Iterator<String> iterator = props.values().iterator();
            while (iterator.hasNext()){
                String next = iterator.next();
                String[] split = StringUtils.split(next, "=", 2);
                if ("addr".equals(split[0]) && StringUtils.isNotBlank(split[1])){
                    HostAndPort hostAndPort = HostAndPort.parseString(split[1]);
                    clientConnection.setHostAndPort(hostAndPort);
                    continue;
                }
                Field declaredField = FieldUtils.getDeclaredField(clientConnectionClass, split[0], true);
                if (declaredField != null) {
                    try {
                        FieldUtils.writeField(declaredField,clientConnection,split[1],true);
                    } catch (IllegalAccessException e) {
                        log.error("redis 客户端信息写入字段[{}],值[{}] 失败",split[0],split[1]);
                    }
                }
            }
        }
        return clientConnections;
    }

    /**
     * 扫描 redis 的 key
     * @param redisScanParam
     * @return
     * @throws IOException
     */
    public RedisScanResult scan(RedisScanParam redisScanParam) throws IOException, ClassNotFoundException {
        String connName = redisScanParam.getConnName();
        Jedis jedis = jedis(connName);
        boolean cluster = isCluster(jedis);

        int limit = redisScanParam.getLimit();
        String pattern = redisScanParam.getPattern();
        String combineCursor = redisScanParam.getCursor();

        JedisCommands client = jedis;

        // 开始搜索每个节点上的 key 列表
        List<NodeScanResult> nodeScanResults = new ArrayList<>();
        String keySerializer = redisScanParam.getKeySerializer();
        Serializer serializer = serializerChoseService.choseSerializer(keySerializer);

        // 游标计算
        String cursor = combineCursor;
        int hostIndex = 0;          // 主机搜索顺序号,默认为 0
        int currentSearchIndex = 0 ;    // 当前搜索主机顺序号
        if (combineCursor.contains("|")){
            hostIndex = NumberUtils.toInt(combineCursor.split("\\|")[0]);
            cursor = combineCursor.split("\\|")[1];
        }

        if(!cluster){
            int index = redisScanParam.getIndex();
            jedis.select(index);
            NodeScanResult nodeScanResult = nodeScan(jedis, pattern, limit, cursor, serializer);
            nodeScanResults.add(nodeScanResult);
        }else{
            JedisCluster jedisCluster = jedisCluster(jedis);
            client = jedisCluster;
            // ip:port => JedisPool
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            List<Jedis> brokers = clusterNodes.values().stream().map(JedisPool::getResource).collect(Collectors.toList());
            // 去掉非 master 节点
            Iterator<Jedis> jedisIterator = brokers.iterator();
            while (jedisIterator.hasNext()){
                Jedis next = jedisIterator.next();
                String role = jedisRole(next);
                if (!"master".equals(role)){
                    jedisIterator.remove();
                }
            }

            // 对 nodes 进行排序,保证搜索顺序
            Collections.sort(brokers,(a,b)-> (a.getClient().getHost()+a.getClient().getPort()).compareTo(b.getClient().getHost()+b.getClient().getPort()));

            // 开始搜索,这里的前端分页为顺序搜索节点, 前端每次会把搜索的主机顺序号 hostIndex 给过来,然后后端返回前端下一次搜索的主机顺序号
            log.info("搜索顺序号为 [{}]",hostIndex);
            Iterator<Jedis> iterator = brokers.iterator();
            label: while (iterator.hasNext()){
                Jedis currentJedis = iterator.next();
                String current = currentJedis.getClient().getHost()+":"+currentJedis.getClient().getPort();
                while (hostIndex -- > 0){
                    currentSearchIndex++;
                    log.info("跳过主机[{}]",current);
                    continue label;
                }
                Integer keySize = nodeScanResults.stream().map(NodeScanResult::getKeys).map(Set::size).reduce(0, (a, b) -> a + b);
                NodeScanResult nodeScanResult = nodeScan(currentJedis, pattern, limit - keySize, cursor, serializer);
                nodeScanResults.add(nodeScanResult);

                if (keySize + nodeScanResult.keys.size() >= limit){
                    break;
                }
                // 如果数据没找全,则游标需要重置
                cursor = "0";
                currentSearchIndex++;
            }

        }

        // 对找到的 key 进行包装
        List<RedisKeyResult> redisKeyResults = new ArrayList<>();
        for (NodeScanResult nodeScanResult : nodeScanResults) {
            Set<String> keys = nodeScanResult.getKeys();
            for (String key : keys) {
                String type = client.type(key);
                Long ttl = client.ttl(key);
                Long pttl = client.ttl(key);
                RedisKeyResult redisKeyResult = new RedisKeyResult(key, type, ttl, pttl);
                long length = keyLength(client, key);
                redisKeyResult.setLength(length);
                if (cluster) {
                    int slot = JedisClusterCRC16.getSlot(key);
                    redisKeyResult.setSlot(slot);
                }
                redisKeyResults.add(redisKeyResult);
            }
        }

        //
        String lastCursor = null;
        HostAndPort target = null;
        if (CollectionUtils.isNotEmpty(nodeScanResults)) {
            NodeScanResult nodeScanResult = nodeScanResults.get(nodeScanResults.size() - 1);
            lastCursor = nodeScanResult.getCursor();
            target = nodeScanResult.getTarget();
        }

        closeCluster(cluster, client);

        return new RedisScanResult(redisKeyResults,target,lastCursor,currentSearchIndex);
    }

    /**
     * 当前 jedis 角色
     * @param currentJedis
     * @return
     */
    public String jedisRole(Jedis currentJedis) {
        String info = currentJedis.info("Replication");
        List<String[]> parser = CommandReply.colonCommandReply.parser(info);
        for (String[] line : parser) {
            if (line.length == 2){
                if ("role".equals(line[0])){
                    return line[1];
                }
            }
        }
        return "";
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
    public HashKeyScanResult hashKeyScan(HashScanParam hashScanParam) throws IOException, ClassNotFoundException {
        String connName = hashScanParam.getConnName();
        String hashKeySerializer = hashScanParam.getHashKeySerializer();
        Serializer hashKeySerializerImpl = serializerChoseService.choseSerializer(hashKeySerializer);
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
        String cursor = hashScanParam.getCursor();
        List<String> hashKeyResult = new ArrayList<>();
        do {
            ScanResult<Map.Entry<byte[], byte[]>> scanResult = null;
            if (cluster){
                scanResult =  ((JedisCluster)client).hscan(keyBytes,cursor.getBytes(),scanParams);
            }else{
                scanResult =  jedis.hscan(keyBytes,cursor.getBytes(), scanParams);
            }

            List<Map.Entry<byte[],byte[]>> result = scanResult.getResult();
            for (Map.Entry<byte[], byte[]> entry : result) {
                hashKeyResult.add(Objects.toString(hashKeySerializerImpl.deserialize(entry.getKey(), ClassLoader.getSystemClassLoader())));
            }
            cursor = scanResult.getStringCursor();
            scanParams.count(limit - result.size());        // 需要保证搜索到的数据量的正确性
        }while (hashKeyResult.size() < limit && NumberUtils.toLong(cursor) != 0L);

        closeCluster(cluster,client);

        return new HashKeyScanResult(hashKeyResult,cursor);
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

    @Data
    private static class NodeScanResult {
        private String cursor;
        private Set<String> keys;
        private HostAndPort target;

        public NodeScanResult(String cursor, Set<String> keys) {
            this.cursor = cursor;
            this.keys = keys;
        }

        public NodeScanResult(String cursor, Set<String> keys, HostAndPort target) {
            this.cursor = cursor;
            this.keys = keys;
            this.target = target;
        }
    }

    /**
     * 单个节点的 key 扫描
     * @param jedis
     * @param pattern
     * @param limit
     * @param keySerializer
     * @return
     */
    private NodeScanResult nodeScan(Jedis jedis, String pattern, int limit,String cursor, Serializer serializer) throws IOException, ClassNotFoundException {
        ScanParams scanParams = new ScanParams();
        scanParams.count(limit);
        if(StringUtils.isNotBlank(pattern)) {
            scanParams.match(pattern);
        }
        //如果搜索结果为空,则继续搜索,直到有值或搜索到末尾
        Set<String> keyAllReuslts = new HashSet<>();
        do {
            ScanResult scanResult = jedis.scan(cursor.getBytes(), scanParams);
            List<byte[]> result = scanResult.getResult();
            for (byte[] bytes : result) {
                keyAllReuslts.add(Objects.toString(serializer.deserialize(bytes, ClassLoader.getSystemClassLoader())));
            }
            cursor = scanResult.getStringCursor();
            scanParams.count(limit - keyAllReuslts.size());
        }while (keyAllReuslts.size() < limit && NumberUtils.toLong(cursor) != 0L);

        String host = jedis.getClient().getHost();
        int port = jedis.getClient().getPort();
        HostAndPort target = new HostAndPort(host, port);
        return new NodeScanResult(cursor,keyAllReuslts,target);
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
        pluginManager.register(PluginDto.builder().module("monitor").name(module).logo("redis.jpg").desc("redis 数据查看,集群信息管理").author("sanri").envs("default").build());
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

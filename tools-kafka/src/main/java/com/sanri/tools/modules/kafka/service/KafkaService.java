package com.sanri.tools.modules.kafka.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.core.dtos.PluginDto;
import com.sanri.tools.modules.core.service.plugin.PluginManager;
import com.sanri.tools.modules.kafka.dtos.*;
import com.sanri.tools.modules.kafka.dtos.MBeanMonitorInfo;
import com.sanri.tools.modules.protocol.exception.ToolException;
import com.sanri.tools.modules.protocol.param.KafkaConnectParam;
import com.sanri.tools.modules.zookeeper.service.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.core.Constants;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * kafka 主题和消费组管理
 */
@Service
@Slf4j
public class KafkaService {
    @Autowired
    private ConnectService connectService;
    @Autowired
    private ZookeeperService zookeeperService;
    @Autowired
    private PluginManager pluginManager;

    public static final String module = "kafka";

    private static final Map<String, AdminClient> adminClientMap = new ConcurrentHashMap<>();

    /**
     * 读取 brokers 信息
     * @param clusterName
     * @return
     * @throws IOException
     */
    public List<BrokerInfo> brokers(String clusterName) throws IOException {
        KafkaConnectParam kafkaConnectParam = (KafkaConnectParam) connectService.readConnParams(module, clusterName);
        String chroot = kafkaConnectParam.getChroot();
        List<BrokerInfo> brokerInfos = readZookeeperBrokers(clusterName, chroot);
        return brokerInfos;
    }

    /**
     * 创建主题
     * @param clusterName
     * @param topic
     * @param partitions
     * @param replication
     * @return
     */
    public void createTopic(String clusterName,String topic,int partitions,int replication) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        NewTopic newTopic = new NewTopic(topic,partitions,(short)replication);
        CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singletonList(newTopic));
        KafkaFuture<Void> voidKafkaFuture = createTopicsResult.values().get(topic);
        voidKafkaFuture.get();
    }

    /**
     * 删除主题
     * @param clusterName
     * @param topic
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void deleteTopic(String clusterName,String topic) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Collections.singletonList(topic));
        deleteTopicsResult.all().get();
    }

    /**
     * 所有主题查询
     *
     * @return
     */
    public List<TopicInfo> topics(String clusterName) throws IOException, ExecutionException, InterruptedException {
        List<TopicInfo> topicInfos = new ArrayList<>();

        AdminClient adminClient = loadAdminClient(clusterName);

        ListTopicsResult listTopicsResult = adminClient.listTopics();
        Set<String> topics = listTopicsResult.names().get();
        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(topics);
        Map<String, KafkaFuture<TopicDescription>> values = describeTopicsResult.values();
        Iterator<Map.Entry<String, KafkaFuture<TopicDescription>>> iterator = values.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, KafkaFuture<TopicDescription>> topicDescriptionEntry = iterator.next();
            String topic = topicDescriptionEntry.getKey();
            TopicDescription topicDescription = topicDescriptionEntry.getValue().get();

            // 复制数据,因为 TopicDescription 没有 getset
            TopicInfo topicInfo = new TopicInfo(topicDescription.name(), topicDescription.isInternal());
            topicInfos.add(topicInfo);
            List<TopicPartitionInfo> partitions = topicDescription.partitions();
            for (TopicPartitionInfo partition : partitions) {
                Node leader = partition.leader();
                BrokerInfo leaderBroker = new BrokerInfo(leader.id(), leader.host(), leader.port());

                List<Node> replicas = partition.replicas();
                List<BrokerInfo> replicaBrokers = new ArrayList<>();
                for (Node replica : replicas) {
                    replicaBrokers.add(new BrokerInfo(replica.id(), replica.host(), replica.port()));
                }

                List<Node> isr = partition.isr();
                List<BrokerInfo> isrBrokers = new ArrayList<>();
                for (Node node : isr) {
                    isrBrokers.add(new BrokerInfo(node.id(), node.host(), node.port()));
                }
                TopicInfo.TopicPartitionInfo partitionInfo = new TopicInfo.TopicPartitionInfo(partition.partition(), leaderBroker, replicaBrokers, isrBrokers);
                topicInfo.addPartitionInfo(partitionInfo);
            }
        }
        return topicInfos;
    }

    /**
     * 查询某个主题的分区数
     * @param clusterName
     * @param topic
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public int partitions(String clusterName, String topic) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singletonList(topic));
        TopicDescription topicDescription = describeTopicsResult.values().get(topic).get();
        return topicDescription.partitions().size();
    }

    /**
     * 查询所有分组
     * @param clusterName
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<String> groups(String clusterName) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        List<String> groupNames = new ArrayList<>();

        ListConsumerGroupsResult listConsumerGroupsResult = adminClient.listConsumerGroups();
        Collection<ConsumerGroupListing> consumerGroupListings = listConsumerGroupsResult.all().get();
        for (ConsumerGroupListing consumerGroupListing : consumerGroupListings) {
            String groupId = consumerGroupListing.groupId();
            groupNames.add(groupId);
        }
        return groupNames;
    }

    /**
     * 删除消费组
     * @param clusterName
     * @param group
     * @return
     */
    public void deleteGroup(String clusterName,String group) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        DeleteConsumerGroupsResult deleteConsumerGroupsResult = adminClient.deleteConsumerGroups(Collections.singletonList(group));
        deleteConsumerGroupsResult.all().get();
    }

    /**
     * 查询分组订阅的主题列表
     * @param clusterName
     * @param group
     * @return
     * @throws IOException
     */
    public Set<String> groupSubscribeTopics(String clusterName, String group) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        Set<String> subscribeTopics = new HashSet<>();

        DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(Collections.singletonList(group));
        ConsumerGroupDescription consumerGroupDescription = describeConsumerGroupsResult.describedGroups().get(group).get();
        // 这里应该可以看到这个分区分配给哪个了
        String partitionAssignor = consumerGroupDescription.partitionAssignor();
        ConsumerGroupState state = consumerGroupDescription.state();

        Collection<MemberDescription> members = consumerGroupDescription.members();
        for (MemberDescription member : members) {
            MemberAssignment assignment = member.assignment();
            Set<TopicPartition> topicPartitions = assignment.topicPartitions();
            Iterator<TopicPartition> iterator = topicPartitions.iterator();
            while (iterator.hasNext()){
                TopicPartition topicPartition = iterator.next();
                subscribeTopics.add(topicPartition.topic());
            }
        }
        return subscribeTopics;
    }

    /**
     * 消费组详情查询, 包含分区策略, 当前的组协调器,和每一个主机分到的消费分区
     * @param clusterName
     * @param group
     * @return
     * @throws IOException
     */
    public ConsumerGroupInfo consumerGroupInfo(String clusterName, String group) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);

        DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(Collections.singletonList(group));
        Map<String, KafkaFuture<ConsumerGroupDescription>> stringKafkaFutureMap = describeConsumerGroupsResult.describedGroups();
        ConsumerGroupDescription consumerGroupDescription = stringKafkaFutureMap.get(group).get();

        Node coordinator = consumerGroupDescription.coordinator();
        BrokerInfo coordinatorBroker = new BrokerInfo(coordinator.id(),coordinator.host(),coordinator.port());
        String partitionAssignor = consumerGroupDescription.partitionAssignor();
        ConsumerGroupInfo consumerGroupInfo = new ConsumerGroupInfo(coordinatorBroker,partitionAssignor);

        Map<String,Set<SimpleTopicPartition>> map = new HashMap<>();

        Collection<MemberDescription> members = consumerGroupDescription.members();
        for (MemberDescription member : members) {
            String host = member.host();                // 需要加入这个,这样才能知道哪些主题的哪些分区在哪个主机上消费
            MemberAssignment assignment = member.assignment();

            Set<TopicPartition> topicPartitions = assignment.topicPartitions();
            Set<SimpleTopicPartition> simpleTopicPartitions = new HashSet<>();
            for (TopicPartition topicPartition : topicPartitions) {
                SimpleTopicPartition simpleTopicPartition = new SimpleTopicPartition(topicPartition.topic(), topicPartition.partition());
                simpleTopicPartitions.add(simpleTopicPartition);
            }
            Set<SimpleTopicPartition> existHostTopicPartitions = map.computeIfAbsent(host, k -> new HashSet<>());
            existHostTopicPartitions.addAll(simpleTopicPartitions);
        }
        Iterator<Map.Entry<String, Set<SimpleTopicPartition>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Set<SimpleTopicPartition>> entry = iterator.next();
            String host = entry.getKey();
            Set<SimpleTopicPartition> simpleTopicPartitions = entry.getValue();
            consumerGroupInfo.addMember(new ConsumerGroupInfo.MemberInfo(host,simpleTopicPartitions));
        }

        return consumerGroupInfo;
    }


    /**
     * 消费组主题信息监控; 单个消费组内,单个主题消费情况的查询
     * @param clusterName
     * @param group
     * @param topic
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<OffsetShow> groupTopicConsumerInfo(String clusterName, String group, String topic) throws IOException, ExecutionException, InterruptedException {
        List<OffsetShow> offsetShows = new ArrayList<>();
        AdminClient adminClient = loadAdminClient(clusterName);
        KafkaConsumer<byte[], byte[]> consumer = loadConsumerClient(clusterName);

        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singletonList(topic));
        TopicDescription topicDescription = describeTopicsResult.values().get(topic).get();
        List<TopicPartitionInfo> partitions = topicDescription.partitions();
        List<TopicPartition> topicPartitions = new ArrayList<>();
        for (TopicPartitionInfo partition : partitions) {
            TopicPartition topicPartition = new TopicPartition(topic, partition.partition());
            topicPartitions.add(topicPartition);
        }

        //查询  offset 信息
        ListConsumerGroupOffsetsOptions listConsumerGroupOffsetsOptions = new ListConsumerGroupOffsetsOptions();
        listConsumerGroupOffsetsOptions.topicPartitions(topicPartitions);

        Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap = adminClient.listConsumerGroupOffsets(group,listConsumerGroupOffsetsOptions).partitionsToOffsetAndMetadata().get();

        Map<TopicPartition, Long> topicPartitionLongMap = consumer.endOffsets(topicPartitions);
        consumer.close();

        Iterator<Map.Entry<TopicPartition, Long>> iterator = topicPartitionLongMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<TopicPartition, Long> entry = iterator.next();
            TopicPartition topicPartition = entry.getKey();
            Long logSize = entry.getValue();
            OffsetAndMetadata offsetAndMetadata = offsetAndMetadataMap.get(topicPartition);
            long offset = offsetAndMetadata.offset();
            int partition = topicPartition.partition();
            long lag = logSize - offset;
            OffsetShow offsetShow = new OffsetShow(topic, partition, offset, logSize);
            offsetShows.add(offsetShow);
        }

        Collections.sort(offsetShows);
        return offsetShows;
    }

    /**
     * 每一个分区的 logSize
     * @param clusterName
     * @param topic
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<TopicLogSize> logSizes(String clusterName, String topic) throws IOException, ExecutionException, InterruptedException {
        List<TopicLogSize> topicLogSizes = new ArrayList<>();

        KafkaConsumer<byte[], byte[]> consumer = loadConsumerClient(clusterName);
        try {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);

            List<TopicPartition> topicPartitions = new ArrayList<>();
            for (int i = 0; i < partitionInfos.size(); i++) {
                topicPartitions.add(new TopicPartition(topic, i));
            }

            Map<TopicPartition, Long> topicPartitionLongMap = consumer.endOffsets(topicPartitions);
            Iterator<Map.Entry<TopicPartition, Long>> iterator = topicPartitionLongMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<TopicPartition, Long> entry = iterator.next();
                TopicPartition topicPartition = entry.getKey();
                Long logSize = entry.getValue();

                topicLogSizes.add(new TopicLogSize(topic,topicPartition.partition(),logSize));
            }
        }finally {
            if(consumer != null)
                consumer.close();
        }

        return topicLogSizes;
    }

    /**
     * 加载一个消费客户端
     * @param clusterName
     * @return
     * @throws IOException
     */
    public KafkaConsumer<byte[], byte[]> loadConsumerClient(String clusterName) throws IOException {
        KafkaConnectParam kafkaConnectParam = (KafkaConnectParam) connectService.readConnParams(module, clusterName);
        Map<String, Object> properties = kafkaConnectParam.getKafka().buildConsumerProperties();
        // 设置为 byte[] 序列化
        properties.put("key.deserializer","org.apache.kafka.common.serialization.ByteArrayDeserializer");
        properties.put("value.deserializer","org.apache.kafka.common.serialization.ByteArrayDeserializer");
        return new KafkaConsumer<byte[], byte[]>(properties);
    }

    public AdminClient loadAdminClient(String clusterName) throws IOException {
        AdminClient adminClient = adminClientMap.get(clusterName);
        if(adminClient == null){
            KafkaConnectParam kafkaConnectParam = (KafkaConnectParam) connectService.readConnParams(module, clusterName);
            KafkaProperties kafka = kafkaConnectParam.getKafka();
            Map<String, Object> kafkaProperties = kafka.buildAdminProperties();
            adminClient = AdminClient.create(kafkaProperties);
            adminClientMap.put(clusterName,adminClient);
        }

        return adminClient;
    }

    private static final String relativeBrokerPath = "/brokers/ids";
    static Pattern ipPort = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+)");
    private List<BrokerInfo> readZookeeperBrokers(String connName,String chroot) throws IOException {
        List<BrokerInfo> brokerInfos = new ArrayList<>();

        List<String> childrens = zookeeperService.childrens(connName, chroot + relativeBrokerPath);
        for (String children : childrens) {
            String brokerInfo = Objects.toString(zookeeperService.readData(connName, chroot + relativeBrokerPath + "/" + children, "string"),"");
            JSONObject brokerJson = JSONObject.parseObject(brokerInfo);
            String host = brokerJson.getString("host");
            int port = brokerJson.getIntValue("port");
            int jmxPort = brokerJson.getIntValue("jmx_port");

            if(StringUtils.isBlank(host)){
                //如果没有提供 host 和 port 信息，则从 endpoints 中拿取信息
                JSONArray endpoints = brokerJson.getJSONArray("endpoints");
                String endpoint = endpoints.getString(0);
                Matcher matcher = ipPort.matcher(endpoint);
                if(matcher.find()) {
                    host = matcher.group(1);
                    port = NumberUtils.toInt(matcher.group(2));
                }
            }

            brokerInfos.add(new BrokerInfo(NumberUtils.toInt(children),host,port,jmxPort));
        }
        return brokerInfos;
    }

    /**
     *  kafka 的 mBean 数据监控
     */
    private static final String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
    public Collection<MBeanMonitorInfo> monitor(String clusterName, Class clazz, String topic) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        KafkaConnectParam kafkaConnectParam = (KafkaConnectParam) connectService.readConnParams(module, clusterName);
        List<BrokerInfo> brokers = readZookeeperBrokers(kafkaConnectParam.getConnectIdParam().getConnName(),kafkaConnectParam.getChroot());

        List<MBeanMonitorInfo> mBeanInfos = new ArrayList<>();
        for (BrokerInfo broker : brokers) {
            String host = broker.getHost();
            int jxmPort = broker.getJxmPort();
            String uri = host+":"+jxmPort;
            if(jxmPort == -1){
                return null;
            }

            JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, uri));
            JMXConnector connector = JMXConnectorFactory.connect(jmxSeriverUrl);
            MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();

            // 遍历所有的 mBean
            Constants constants = new Constants(clazz);
            List<String> mBeans = constansValues(constants);
            for (String mBean : mBeans) {
                if (clazz == BrokerTopicMetrics.TopicMetrics.class){
                    mBean = String.format(mBean,topic);
                }
                Object fifteenMinuteRate = mbeanConnection.getAttribute(new ObjectName(mBean), BrokerTopicMetrics.MBean.FIFTEEN_MINUTE_RATE);
                Object fiveMinuteRate = mbeanConnection.getAttribute(new ObjectName(mBean), BrokerTopicMetrics.MBean.FIVE_MINUTE_RATE);
                Object meanRate = mbeanConnection.getAttribute(new ObjectName(mBean), BrokerTopicMetrics.MBean.MEAN_RATE);
                Object oneMinuteRate = mbeanConnection.getAttribute(new ObjectName(mBean), BrokerTopicMetrics.MBean.ONE_MINUTE_RATE);
                MBeanMonitorInfo mBeanInfo = new MBeanMonitorInfo(mBean,objectDoubleValue(fifteenMinuteRate), objectDoubleValue(fiveMinuteRate), objectDoubleValue(meanRate), objectDoubleValue(oneMinuteRate));
                mBeanInfos.add(mBeanInfo);
            }
        }

        // 数据合并
        Map<String, MBeanMonitorInfo> mergeMap = new HashMap<>();
        for (MBeanMonitorInfo mBeanInfo : mBeanInfos) {
            String mBean = mBeanInfo.getmBean();
            MBeanMonitorInfo mergeMBeanInfo = mergeMap.get(mBean);
            if(mergeMBeanInfo == null){
                mergeMBeanInfo = mBeanInfo;
                mergeMap.put(mBean,mergeMBeanInfo);
                continue;
            }
            mergeMBeanInfo.addData(mBeanInfo);
        }

        return mergeMap.values();
    }

    private double objectDoubleValue(Object value){
        return  NumberUtils.toDouble(value.toString());
    }

    private List<String> constansValues(Constants constants) {
        List<String> mMbeans = new ArrayList<>();
        try {
            Method getFieldCache = Constants.class.getDeclaredMethod("getFieldCache");
            getFieldCache.setAccessible(true);
            Map<String, Object> invokeMethod = (Map<String, Object>) ReflectionUtils.invokeMethod(getFieldCache, constants);
            Collection<Object> values = invokeMethod.values();

            for (Object value : values) {
                mMbeans.add(Objects.toString(value));
            }
        } catch (NoSuchMethodException e) {}
        return mMbeans;
    }

    @PostConstruct
    public void register(){
        pluginManager.register(PluginDto.builder().module(module).name("main").author("sanri").envs("default").build());
    }

    @PreDestroy
    public void destory(){
        log.info("清除 {} 客户端列表:{}",module,adminClientMap.keySet());
        Iterator<AdminClient> iterator = adminClientMap.values().iterator();
        while (iterator.hasNext()){
            AdminClient next = iterator.next();
            if(next != null){
                try {
                    next.close();
                } catch (Exception e) {}
            }
        }
    }

    /**
     * 创建 kafka 连接,强依赖于 zookeeper
     * @param kafkaConnectParam
     */
    public void createConnect(KafkaConnectParam kafkaConnectParam) throws IOException {
        KafkaProperties kafka = kafkaConnectParam.getKafka();
        String connName = kafkaConnectParam.getConnectIdParam().getConnName();
        String chroot = kafkaConnectParam.getChroot();

        List<String> bootstrapServers = kafka.getBootstrapServers();
        if (bootstrapServers.size() == 1){
            String brokerOnlyOne = bootstrapServers.get(0);
            if ("localhost:9092".equals(brokerOnlyOne)){
                // 如果是默认的,检查 zookeeper 上的节点,如果不一致,则取 zookeeper 上的节点数据
                List<BrokerInfo> brokers = readZookeeperBrokers(connName,chroot);
                if (brokers.size() == 0){
                    throw new ToolException("zookeeper "+connName+" 上的 kafka 节点为空");
                }

                List<String> bootstrapServersZookeeper = brokers.stream().map(broker -> broker.getHost() + ":" + broker.getPort()).collect(Collectors.toList());
                String bootstrapServersString = StringUtils.join(bootstrapServersZookeeper, ',');
                if (!bootstrapServersString.equals(brokerOnlyOne)){
                    kafka.setBootstrapServers(bootstrapServersZookeeper);
                }
            }
        }

        // 一些默认参数配置
        KafkaProperties.Consumer consumer = kafka.getConsumer();
        consumer.setGroupId("console-sanritools-"+connName);
        consumer.setAutoOffsetReset("earliest");
        consumer.setEnableAutoCommit(true);

        // 然后调用 连接服务,将配置序列化
        connectService.createConnect(module, null);
    }

}

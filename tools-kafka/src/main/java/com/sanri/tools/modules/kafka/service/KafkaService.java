package com.sanri.tools.modules.kafka.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;
import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sanri.tools.modules.core.dtos.UpdateConnectEvent;
import com.sanri.tools.modules.core.dtos.param.RedisConnectParam;
import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectInput;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectOutput;
import com.sanri.tools.modules.core.service.connect.events.SecurityConnectEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Constants;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.tools.modules.core.dtos.param.KafkaConnectParam;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.file.ConnectServiceOldFileBase;

import com.sanri.tools.modules.kafka.dtos.*;
import com.sanri.tools.modules.zookeeper.service.ZookeeperService;

import lombok.extern.slf4j.Slf4j;

/**
 * kafka ????????????????????????
 */
@Service
@Slf4j
public class KafkaService implements ApplicationListener<SecurityConnectEvent> {
    @Autowired
    private ConnectService connectService;
    @Autowired
    private ZookeeperService zookeeperService;

    private YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();

    public static final String MODULE = "kafka";

    private static final Map<String, AdminClient> adminClientMap = new ConcurrentHashMap<>();

    /**
     * ???????????????????????????, ???????????????????????????
     * @param clusterName
     */
    public void stopAndRemove(String clusterName){
        final AdminClient adminClient = adminClientMap.get(clusterName);
        if (adminClient != null){
            try {
                adminClient.close();
            }finally {
                // ??????????????????
                adminClientMap.remove(clusterName);
            }
        }
    }

    /**
     * ?????? brokers ??????
     * @param clusterName
     * @return
     * @throws IOException
     */
    public List<BrokerInfo> brokers(String clusterName) throws IOException {
//        KafkaConnectParam kafkaConnectParam = (KafkaConnectParam) connectService.readConnParams(MODULE, clusterName);
        KafkaConnectParam kafkaConnectParam = convertToKafkaConnectParam(clusterName);
        String chroot = kafkaConnectParam.getChroot();
        List<BrokerInfo> brokerInfos = readZookeeperBrokers(clusterName, chroot);
        return brokerInfos;
    }

    /**
     * ??????????????????, ???????????? KafkaConnectParam
     * @param clusterName
     * @return
     * @throws IOException
     */
    KafkaConnectParam convertToKafkaConnectParam(String clusterName) throws IOException {
        final String loadContent = connectService.loadContent(MODULE, clusterName);
        ByteArrayResource byteArrayResource = new ByteArrayResource(loadContent.getBytes(StandardCharsets.UTF_8));
        final List<PropertySource<?>> load = yamlPropertySourceLoader.load("a", byteArrayResource);
        Iterable<ConfigurationPropertySource> from = ConfigurationPropertySources.from(load);
        Binder binder = new Binder(from);
        BindResult<KafkaConnectParam> bind = binder.bind("", KafkaConnectParam.class);
        KafkaConnectParam kafkaConnectParam = bind.get();
        return kafkaConnectParam;
    }

    /**
     * ????????????
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
     * ????????????
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
     * ??????????????????
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

            // ????????????,?????? TopicDescription ?????? getset
            TopicInfo topicInfo = new TopicInfo(topicDescription.name(), topicDescription.isInternal());
            topicInfos.add(topicInfo);
            List<TopicPartitionInfo> partitions = topicDescription.partitions();
            for (TopicPartitionInfo partition : partitions) {
                Node leader = partition.leader();
                BrokerInfo leaderBroker = null;
                if (leader != null){
                    leaderBroker = new BrokerInfo(leader.id(), leader.host(), leader.port());
                }else{
                    log.warn("??????[{}],??????[{}] ??? leader ??????",topic,partition.partition());
                }

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
     * ??????????????????????????????
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
     * ??????????????????
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
     * ???????????????
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
     * ?????????????????????????????????
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
        // ??????????????????????????????????????????????????????
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
     * ?????????????????????, ??????????????????, ?????????????????????,???????????????????????????????????????
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
            // ??????????????????,?????????????????????????????????????????????????????????????????????
            String host = member.host();
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
     * ???????????????????????????????????????????????????
     * @param clusterName
     * @param group
     * @return
     */
    public List<TopicOffset> groupTopicConsumerInfos(String clusterName, String group) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = adminClient.listConsumerGroupOffsets(group).partitionsToOffsetAndMetadata().get();
        Set<TopicPartition> topicPartitions = topicPartitionOffsetAndMetadataMap.keySet();
        KafkaConsumer<byte[], byte[]> kafkaConsumer = loadConsumerClient(clusterName);
        Map<TopicPartition, Long> topicPartitionLongMap = kafkaConsumer.endOffsets(topicPartitions);

        List<OffsetShow> offsetShows = new ArrayList<>();
        Iterator<Map.Entry<TopicPartition, OffsetAndMetadata>> iterator = topicPartitionOffsetAndMetadataMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<TopicPartition, OffsetAndMetadata> offsetAndMetadataEntry = iterator.next();
            TopicPartition topicPartition = offsetAndMetadataEntry.getKey();
            OffsetAndMetadata offsetAndMetadata = offsetAndMetadataEntry.getValue();
            Long logSize = topicPartitionLongMap.get(topicPartition);
            OffsetShow offsetShow = new OffsetShow(topicPartition.topic(), topicPartition.partition(), offsetAndMetadata.offset(),logSize);
            offsetShows.add(offsetShow);
        }

        Map<String, List<OffsetShow>> listMap = offsetShows.stream().collect(Collectors.groupingBy(OffsetShow::getTopic));

        // ??????????????????
        List<TopicOffset> topicOffsets = new ArrayList<>();
        Iterator<Map.Entry<String, List<OffsetShow>>> listMapIterator = listMap.entrySet().iterator();
        while (listMapIterator.hasNext()){
            Map.Entry<String, List<OffsetShow>> entry = listMapIterator.next();
            String topic = entry.getKey();
            List<OffsetShow> offsetShowList = entry.getValue();
            TopicOffset topicOffset = new TopicOffset(group, topic,offsetShowList);

            long totalLogSize = 0 , totalOffset = 0 ;
            for (OffsetShow offsetShow : offsetShowList) {
                long logSize = offsetShow.getLogSize();
                long offset = offsetShow.getOffset();
                totalLogSize += logSize;
                totalOffset += offset;
            }
            topicOffset.setLogSize(totalLogSize);
            topicOffset.setOffset(totalOffset);
            topicOffset.setLag(totalLogSize - totalOffset);
            topicOffsets.add(topicOffset);
        }

        return topicOffsets;
    }


    /**
     * ???????????????????????????; ??????????????????,?????????????????????????????????
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

        //??????  offset ??????
        ListConsumerGroupOffsetsOptions listConsumerGroupOffsetsOptions = new ListConsumerGroupOffsetsOptions();
        listConsumerGroupOffsetsOptions.topicPartitions(topicPartitions);

        Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap = adminClient.listConsumerGroupOffsets(group,listConsumerGroupOffsetsOptions).partitionsToOffsetAndMetadata().get();

        Map<TopicPartition, Long> topicPartitionLongMap = consumer.endOffsets(topicPartitions);
        Map<TopicPartition, Long> beginningTopicPartitionLongMap = consumer.beginningOffsets(topicPartitions);
        consumer.close();

        Iterator<Map.Entry<TopicPartition, Long>> iterator = topicPartitionLongMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<TopicPartition, Long> entry = iterator.next();
            TopicPartition topicPartition = entry.getKey();
            Long minOffset = beginningTopicPartitionLongMap.get(topicPartition);
            Long logSize = entry.getValue();
            OffsetAndMetadata offsetAndMetadata = offsetAndMetadataMap.get(topicPartition);
            long offset = offsetAndMetadata.offset();
            int partition = topicPartition.partition();
            long lag = logSize - offset;
            OffsetShow offsetShow = new OffsetShow(topic, partition, offset, logSize);
            offsetShow.setMinOffset(minOffset);
            offsetShows.add(offsetShow);
        }

        Collections.sort(offsetShows);
        return offsetShows;
    }

    /**
     * ?????????????????? logSize
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
            // ??????????????????,????????? zk ????????????????????????
//            KafkaConnectParam kafkaConnectParam = (KafkaConnectParam) connectService.readConnParams(MODULE, clusterName);
            KafkaConnectParam kafkaConnectParam = convertToKafkaConnectParam(clusterName);
            int partitions = 0;
//            try {
//                partitions = zookeeperService.countChildren(clusterName, kafkaConnectParam.getChroot() + "/brokers/topics/" + topic + "/partitions");
//            }catch (Exception e){
//                log.error("[{}]??? zookeeper ?????????[{}]???????????????,??????????????????,??? kafka ?????????",clusterName,topic);
//                List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
//                partitions = partitionInfos.size();
//            }
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            partitions = partitionInfos.size();

            List<TopicPartition> topicPartitions = new ArrayList<>();
            for (int i = 0; i < partitions; i++) {
                topicPartitions.add(new TopicPartition(topic, i));
            }

            Map<TopicPartition, Long> topicPartitionLongMap = consumer.endOffsets(topicPartitions);
            Map<TopicPartition, Long> beginningTopicPartitionLongMap = consumer.beginningOffsets(topicPartitions);
            consumer.assign(topicPartitions);

            int hasDataPartitions = 0 ;
            Iterator<Map.Entry<TopicPartition, Long>> iterator = topicPartitionLongMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<TopicPartition, Long> entry = iterator.next();
                TopicPartition topicPartition = entry.getKey();
                Long minOffset = beginningTopicPartitionLongMap.get(topicPartition);
                Long logSize = entry.getValue();

                // ???????????????,???????????????????????? ,????????????????????????????????????
                if (!logSize.equals(minOffset)){
                    consumer.seek(topicPartition,logSize - 1);
                    // ??????????????????????????????????????????, ????????????????????????
                    hasDataPartitions ++;
                }

                topicLogSizes.add(new TopicLogSize(topic,topicPartition.partition(),logSize,minOffset));
            }

            // ?????????????????????????????????,????????????????????????
            int loadTimes = 5; // ?????? 5 ???,?????? 10 ms ???????????????????????????
            Map<Integer, Long> partitionLastTime = new HashMap<>();
            while (hasDataPartitions != 0 && loadTimes -- > 0) {
                log.info("??? [{}] ????????? [{}] ????????????????????????????????? ",(5 - loadTimes),topic);
                ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(Duration.ofMillis(20));
                Iterator<ConsumerRecord<byte[], byte[]>> recordIterator = consumerRecords.iterator();
                while (recordIterator.hasNext()) {
                    ConsumerRecord<byte[], byte[]> consumerRecord = recordIterator.next();
                    int partition = consumerRecord.partition();
                    long timestamp = consumerRecord.timestamp();
                    partitionLastTime.put(partition, timestamp);
                    // ???????????????????????????????????????
                    hasDataPartitions--;
                }
            }
            for (TopicLogSize topicLogSize : topicLogSizes) {
                int partition = topicLogSize.getPartition();
                Long timestamp = partitionLastTime.get(partition);
                if (timestamp != null) {
                    topicLogSize.setTimestamp(timestamp);
                }
            }
        }finally {
            if(consumer != null) {
                consumer.close();
            }
        }
        Collections.sort(topicLogSizes,(a,b) -> a.getPartition() - b.getPartition());
        return topicLogSizes;
    }

    /**
     * ???????????????????????????
     * @param clusterName
     * @return
     * @throws IOException
     */
    public KafkaConsumer<byte[], byte[]> loadConsumerClient(String clusterName) throws IOException {
//        KafkaConnectParam kafkaConnectParam = (KafkaConnectParam) connectService.readConnParams(MODULE, clusterName);
        final KafkaConnectParam kafkaConnectParam = convertToKafkaConnectParam(clusterName);
        Map<String, Object> properties = kafkaConnectParam.getKafka().buildConsumerProperties();
        // ????????? byte[] ?????????
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.ByteArrayDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.ByteArrayDeserializer");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"console-"+clusterName);
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,"30000");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"true");
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"500");
//        properties.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG,"1000");
        return new KafkaConsumer<byte[], byte[]>(properties);
    }

    public AdminClient loadAdminClient(String clusterName) throws IOException {
        AdminClient adminClient = adminClientMap.get(clusterName);
        if(adminClient == null){
//            KafkaConnectParam kafkaConnectParam = (KafkaConnectParam) connectService.readConnParams(MODULE, clusterName);
            final KafkaConnectParam kafkaConnectParam = convertToKafkaConnectParam(clusterName);
            KafkaProperties kafka = kafkaConnectParam.getKafka();
            Map<String, Object> kafkaProperties = kafka.buildAdminProperties();
            log.info("kafka connect properties:\n {}",kafkaProperties);
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
                //?????????????????? host ??? port ??????????????? endpoints ???????????????
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
     *  kafka ??? mBean ????????????
     */
    private static final String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
    public Collection<MBeanMonitorInfo> monitor(String clusterName, Class clazz, String topic) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
//        KafkaConnectParam kafkaConnectParam = (KafkaConnectParam) connectService.readConnParams(MODULE, clusterName);
        final KafkaConnectParam kafkaConnectParam = convertToKafkaConnectParam(clusterName);
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

            // ??????????????? mBean
            Constants constants = new Constants(clazz);
            List<String> mBeans = constansValues(constants);
            try {
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
            } catch (InstanceNotFoundException e) {
                log.error("????????????[{}]??????[{}]????????????????????? [{}]",clusterName,topic,e.getMessage());
            }
        }

        // ????????????
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

    private BigDecimal objectDoubleValue(Object value){
        return new BigDecimal(value.toString());
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

    @PreDestroy
    public void destory(){
        log.info("?????? {} ???????????????:{}", MODULE,adminClientMap.keySet());
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

    @Override
    public void onApplicationEvent(SecurityConnectEvent securityConnectEvent) {
        ConnectOutput connectOutput = (ConnectOutput) securityConnectEvent.getSource();
        final ConnectInput connectInput = connectOutput.getConnectInput();
        if (MODULE.equals(connectInput.getModule())){
            String connName = connectInput.getBaseName();
            final AdminClient adminClient = adminClientMap.remove(connName);
            if (adminClient != null){
                // kafka ???????????????????????????, ?????????????????????
                CompletableFuture.runAsync(() -> {adminClient.close();});
            }
            log.info("[{}]??????[{}]????????????,?????????????????????????????????", MODULE,connName);
        }
    }

    /**
     * ?????? kafka ??????,???????????? zookeeper
     * @param kafkaConnectParam
     */
//    public void createConnect(KafkaConnectParam kafkaConnectParam) throws IOException {
//        KafkaProperties kafka = kafkaConnectParam.getKafka();
//        String connName = kafkaConnectParam.getConnectIdParam().getConnName();
//        String chroot = kafkaConnectParam.getChroot();
//
//        List<String> bootstrapServers = kafka.getBootstrapServers();
//        if (bootstrapServers.size() == 1){
//            String brokerOnlyOne = bootstrapServers.get(0);
//            if ("localhost:9092".equals(brokerOnlyOne)){
//                // ??????????????????,?????? zookeeper ????????????,???????????????,?????? zookeeper ??????????????????
//                List<BrokerInfo> brokers = readZookeeperBrokers(connName,chroot);
//                if (brokers.size() == 0){
//                    throw new ToolException("zookeeper "+connName+" ?????? kafka ????????????");
//                }
//
//                List<String> bootstrapServersZookeeper = brokers.stream().map(broker -> broker.getHost() + ":" + broker.getPort()).collect(Collectors.toList());
//                String bootstrapServersString = StringUtils.join(bootstrapServersZookeeper, ',');
//                if (!bootstrapServersString.equals(brokerOnlyOne)){
//                    kafka.setBootstrapServers(bootstrapServersZookeeper);
//                }
//            }
//        }
//
//        // ????????????????????????
//        KafkaProperties.Consumer consumer = kafka.getConsumer();
//        consumer.setGroupId("console-sanritools-"+connName);
//        consumer.setAutoOffsetReset("earliest");
//        consumer.setEnableAutoCommit(true);
//
//        // ???????????? ????????????,??????????????????
////        connectService.createConnect(MODULE, JSON.toJSONString(kafkaConnectParam));
//    }

}

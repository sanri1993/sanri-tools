package systest;

import io.netty.channel.DefaultChannelId;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.admin.TopicOffset;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.GroupList;
import org.apache.rocketmq.common.protocol.body.KVTable;
import org.apache.rocketmq.common.protocol.body.TopicList;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.route.QueueData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class RocketMqMain {

    DefaultMQAdminExt mqAdminExt = new DefaultMQAdminExt();

    @Before
    public void before() throws MQClientException {
        mqAdminExt.setNamesrvAddr("192.168.60.180:9876");
        mqAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        long startTime = System.currentTimeMillis();
        // 里面静态代码耗时, 导致超时, 所以先执行下静态代码
        DefaultChannelId.newInstance();
        log.info("新建 channel 实例耗时: {}",System.currentTimeMillis() - startTime);
        mqAdminExt.start();
    }


    @Test
    public void testMethods() throws InterruptedException, RemotingException, UnsupportedEncodingException, MQBrokerException, MQClientException {
        final Properties brokerConfig = mqAdminExt.getBrokerConfig("192.168.60.180:10911");
        System.out.println(brokerConfig);

        String testTopic = "FSSC2D171_YN_CONSOLE_ROLE";
        String consumerGroup = "ANQIXIANG21081901_YN_MQCREATEUSERCONSUMER_ECS_CONSOLE_CREATEUSER";

        final TopicStatsTable topicStatsTable = mqAdminExt.examineTopicStats(testTopic);
        final Iterator<Map.Entry<MessageQueue, TopicOffset>> iterator = topicStatsTable.getOffsetTable().entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry<MessageQueue, TopicOffset> offsetEntry = iterator.next();
            final MessageQueue messageQueue = offsetEntry.getKey();
            final TopicOffset topicOffset = offsetEntry.getValue();

            log.info("topic:{},queue: {}, broker: {},min: {},max:{}, updateTime: {}",messageQueue.getTopic(),messageQueue.getQueueId(),messageQueue.getBrokerName(),topicOffset.getMaxOffset(),topicOffset.getMaxOffset(),topicOffset.getLastUpdateTimestamp());
        }

        final TopicRouteData topicRouteData = mqAdminExt.examineTopicRouteInfo(testTopic);
        final List<QueueData> queueDatas = topicRouteData.getQueueDatas();
        for (QueueData queueData : queueDatas) {
            final String brokerName = queueData.getBrokerName();
            final int readQueueNums = queueData.getReadQueueNums();
            final int writeQueueNums = queueData.getWriteQueueNums();
            final int perm = queueData.getPerm();
            final int topicSynFlag = queueData.getTopicSynFlag();

            log.info("{}:{}:{}:{}:{}",brokerName,readQueueNums,writeQueueNums,perm,topicSynFlag);
        }
        final List<BrokerData> brokerDatas = topicRouteData.getBrokerDatas();
        for (BrokerData brokerData : brokerDatas) {
            final String cluster = brokerData.getCluster();
            final String brokerName = brokerData.getBrokerName();
            log.info("当前集群: {}, broker: {}",cluster,brokerName);
        }
        final String orderTopicConf = topicRouteData.getOrderTopicConf();
        log.info("orderTopicConf: {}",orderTopicConf);
        final HashMap<String, List<String>> filterServerTable = topicRouteData.getFilterServerTable();
        System.out.println(filterServerTable);

        final GroupList groupList = mqAdminExt.queryTopicConsumeByWho(testTopic);
        final HashSet<String> groupList1 = groupList.getGroupList();


    }

    /**************************************************************************************/

    /**
     * 集群信息, 数据结构为
     * 集群名
     *  broker1 => broker 信息
     *  broker2 => broker 信息
     */
    @Test
    public void testClusterInfo() throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        ClusterInfo clusterInfo = mqAdminExt.examineBrokerClusterInfo();
//        System.out.println(clusterInfo);

        final HashMap<String, Set<String>> clusterAddrTable = clusterInfo.getClusterAddrTable();
        final HashMap<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();

        // cluster => brokerName => brokerData
        final Iterator<Map.Entry<String, Set<String>>> iterator = clusterAddrTable.entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry<String, Set<String>> entry = iterator.next();
            final String clusterName = entry.getKey();
            final Set<String> brokers = entry.getValue();
            for (String broker : brokers) {
                final BrokerData brokerData = brokerAddrTable.get(broker);

            }
        }
    }

    /**
     * broker 运行时数据
     * key = value
     */
    @Test
    public void brokerRuntimeData() throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        final KVTable kvTable = mqAdminExt.fetchBrokerRuntimeStats("192.168.60.180:10911");
        final HashMap<String, String> table = kvTable.getTable();
        System.out.println(table);
    }

    /**
     * 系统主题查询
     */
    @Test
    public void systemTopics() throws MQClientException, RemotingException, InterruptedException {
        DefaultMQProducer producer = new DefaultMQProducer(MixAll.SELF_TEST_PRODUCER_GROUP);
        producer.setInstanceName(String.valueOf(System.currentTimeMillis()));
        producer.setNamesrvAddr("192.168.60.180:9876");
        producer.start();
        final TopicList systemTopicList = producer.getDefaultMQProducerImpl().getmQClientFactory().getMQClientAPIImpl().getSystemTopicList(20000L);
        final Set<String> topicList = systemTopicList.getTopicList();
        System.out.println(topicList);
    }


    /**
     * 所有主题查询
     */
    @Test
    public void testTopics() throws RemotingException, MQClientException, InterruptedException {
        final TopicList topicList = mqAdminExt.fetchAllTopicList();
        final Set<String> topicList1 = topicList.getTopicList();
        // % 开头为重试消息
        // FSSC2D171_YN_CONSOLE_ROLE
        List<String> realTopics = topicList1.stream().filter(topic -> !topic.startsWith("%")).collect(Collectors.toList());
        System.out.println(realTopics);
    }

    /**
     * 主题状态查询
     */
    @Test
    public void topicStat() throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        final TopicStatsTable topicStatsTable = mqAdminExt.examineTopicStats("FSSC2D171_YN_CONSOLE_ROLE");
        final Iterator<Map.Entry<MessageQueue, TopicOffset>> iterator = topicStatsTable.getOffsetTable().entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry<MessageQueue, TopicOffset> offsetEntry = iterator.next();
            final MessageQueue messageQueue = offsetEntry.getKey();
            final TopicOffset topicOffset = offsetEntry.getValue();

            log.info("topic:{},queue: {}, broker: {},min: {},max:{}, updateTime: {}",messageQueue.getTopic(),messageQueue.getQueueId(),messageQueue.getBrokerName(),topicOffset.getMaxOffset(),topicOffset.getMaxOffset(),topicOffset.getLastUpdateTimestamp());
        }
    }

    /**
     * 主题路由信息查询, 结构信息为
     * brokerDatas: broker 数据
     * 队列信息: 即主题配置 读队列数, 写队列数, 权限信息
     */
    @Test
    public void topicRouteInfo() throws RemotingException, MQClientException, InterruptedException {
        final TopicRouteData topicRouteData = mqAdminExt.examineTopicRouteInfo("FSSC2D171_YN_CONSOLE_ROLE");
        final List<QueueData> queueDatas = topicRouteData.getQueueDatas();
        for (QueueData queueData : queueDatas) {
            final String brokerName = queueData.getBrokerName();
            final int readQueueNums = queueData.getReadQueueNums();
            final int writeQueueNums = queueData.getWriteQueueNums();
            final int perm = queueData.getPerm();
            final int topicSynFlag = queueData.getTopicSynFlag();

            log.info("{}:{}:{}:{}:{}",brokerName,readQueueNums,writeQueueNums,perm,topicSynFlag);
        }
        final List<BrokerData> brokerDatas = topicRouteData.getBrokerDatas();
        for (BrokerData brokerData : brokerDatas) {
            final String cluster = brokerData.getCluster();
            final String brokerName = brokerData.getBrokerName();
            log.info("当前集群: {}, broker: {}",cluster,brokerName);
        }
        final String orderTopicConf = topicRouteData.getOrderTopicConf();
        log.info("orderTopicConf: {}",orderTopicConf);
        final HashMap<String, List<String>> filterServerTable = topicRouteData.getFilterServerTable();
        System.out.println(filterServerTable);
    }

    /**
     * 根据主题名称查消息组信息
     */
    @Test
    public void consumerByTopic() throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        final GroupList groupList = mqAdminExt.queryTopicConsumeByWho("1076596_YN_CONSOLE_USER_ROLE");
        System.out.println(groupList.getGroupList());
    }

    /**
     * 消费组状态查询
     */
    @Test
    public void consumerStat() throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        final ConsumeStats consumeStats = mqAdminExt.examineConsumeStats("1076596_YN_MQCONSUMERROLEUSERSERVICE_CONSOLE_USER_ROLE");
        final double consumeTps = consumeStats.getConsumeTps();
        System.out.println(consumeTps);
        final HashMap<MessageQueue, OffsetWrapper> offsetTable = consumeStats.getOffsetTable();
        final Iterator<Map.Entry<MessageQueue, OffsetWrapper>> iterator = offsetTable.entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry<MessageQueue, OffsetWrapper> entry = iterator.next();
            final MessageQueue messageQueue = entry.getKey();
            final OffsetWrapper offsetWrapper = entry.getValue();

            final String topic = messageQueue.getTopic();
            final int queueId = messageQueue.getQueueId();
            final long brokerOffset = offsetWrapper.getBrokerOffset();
            final long consumerOffset = offsetWrapper.getConsumerOffset();
            final long lastTimestamp = offsetWrapper.getLastTimestamp();

            log.info("topic: {},queue: {},brokerOffset: {},consumerOffset: {},lastTime: {}",topic,queueId,brokerOffset,consumerOffset,lastTimestamp);
        }
    }

    /**
     * 消费组某个主题的消费状态查询
     */
    @Test
    public void consumerTopicStat() throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        final ConsumeStats consumeStats = mqAdminExt.examineConsumeStats("1076596_YN_MQCONSUMERROLEUSERSERVICE_CONSOLE_USER_ROLE", "1076596_YN_CONSOLE_USER_ROLE");

    }

}

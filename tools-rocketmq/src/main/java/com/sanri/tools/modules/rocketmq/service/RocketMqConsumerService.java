package com.sanri.tools.modules.rocketmq.service;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

import com.sanri.tools.modules.rocketmq.service.dtos.BrokerMaster;
import com.sanri.tools.modules.rocketmq.service.dtos.BrokerSubscribeConfig;
import com.sanri.tools.modules.rocketmq.service.dtos.ConsumerTopic;
import com.sanri.tools.modules.rocketmq.service.dtos.ResetOffsetRequest;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.common.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RocketMqConsumerService {
    @Autowired
    private RocketMqService rocketMqService;

    @Autowired
    private RocketMqClusterService rocketMqClusterService;

    /**
     * 消费组列表查询
     * @param connName
     * @return
     * @throws Exception
     */
    public Set<String> consumerGroups(String connName) throws Exception{
        final DefaultMQAdminExt mqAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        Set<String> consumerGroupSet = Sets.newHashSet();
        ClusterInfo clusterInfo = mqAdminExt.examineBrokerClusterInfo();
        for (BrokerData brokerData : clusterInfo.getBrokerAddrTable().values()) {
            SubscriptionGroupWrapper subscriptionGroupWrapper = mqAdminExt.getAllSubscriptionGroup(brokerData.selectBrokerAddr(), 6000L);
            final ConcurrentMap<String, SubscriptionGroupConfig> subscriptionGroupTable = subscriptionGroupWrapper.getSubscriptionGroupTable();
            consumerGroupSet.addAll(subscriptionGroupTable.keySet());
        }
        return consumerGroupSet;
    }

    /**
     * 消费组消费状态查询
     * @param connName 连接名
     * @param consumerGroup 消费组
     * @throws Exception
     * @return
     */
    public ConsumeStats consumerStat(String connName, String consumerGroup) throws Exception{
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final ConsumeStats consumeStats = defaultMQAdminExt.examineConsumeStats(consumerGroup);
        return consumeStats;
    }

    /**
     * 消费组订阅主题列表
     * @param connName
     * @param consumerGroup
     * @return
     */
    public Set<String> subscribes(String connName, String consumerGroup) throws Exception{
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final ConsumeStats consumeStats = defaultMQAdminExt.examineConsumeStats(consumerGroup);
        final Iterator<MessageQueue> iterator = consumeStats.getOffsetTable().keySet().iterator();
        Set<String> topics = new HashSet<>();
        while (iterator.hasNext()){
            topics.add(iterator.next().getTopic());
        }
        return topics;
    }

    /**
     * 订阅的主题信息
     * @param connName 连接名
     * @param consumerGroup 消费组名
     * @return
     * @throws Exception
     */
    public Collection<ConsumerTopic> subscribeTopics(String connName, String consumerGroup) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final ConsumeStats consumeStats = defaultMQAdminExt.examineConsumeStats(consumerGroup);

        Map<String, ConsumerTopic> consumerTopicHashMap = new HashMap<>();

        final Iterator<Map.Entry<MessageQueue, OffsetWrapper>> iterator = consumeStats.getOffsetTable().entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry<MessageQueue, OffsetWrapper> offsetWrapperEntry = iterator.next();
            final MessageQueue messageQueue = offsetWrapperEntry.getKey();
            final OffsetWrapper offsetWrapper = offsetWrapperEntry.getValue();

            final String topic = messageQueue.getTopic();
            final ConsumerTopic consumerTopic = consumerTopicHashMap.computeIfAbsent(topic, k -> new ConsumerTopic(k));
            final ConsumerTopic.QueueOffset queueOffset = new ConsumerTopic.QueueOffset(messageQueue.getBrokerName(), messageQueue.getQueueId(), offsetWrapper);
            consumerTopic.getQueueOffsets().add(queueOffset);
        }

        return consumerTopicHashMap.values();
    }

    /**
     * 消费组的消费模式
     * @param connName
     * @param consumerGroup
     * @return
     */
    public ConsumerConnection consumerGroupConnection(String connName, String consumerGroup) throws Exception{
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final ConsumerConnection consumerConnection = defaultMQAdminExt.examineConsumerConnectionInfo(consumerGroup);
        return consumerConnection;
    }

    /**
     * consumerGroup 运行时数据
     * @param connName
     * @param consumerGroup
     * @param clientId
     * @param jstack
     * @return
     * @throws Exception
     */
    public ConsumerRunningInfo consumerGroupRunningInfo(String connName, String consumerGroup, String clientId, boolean jstack) throws Exception{
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final ConsumerRunningInfo consumerRunningInfo = defaultMQAdminExt.getConsumerRunningInfo(consumerGroup, clientId, jstack);
        return consumerRunningInfo;
    }

    /**
     * 消费组配置详情
     * @param connName
     * @param clusterName
     * @param consumerGroup
     * @return
     * @throws Exception
     */
    public List<BrokerSubscribeConfig> consumerGroupConfig(String connName, String clusterName, String consumerGroup) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);

        List<BrokerSubscribeConfig> brokerSubscribeConfigs = new ArrayList<>();
        final List<BrokerMaster> brokerAddrs = rocketMqClusterService.fetchMastersInCluster(connName, clusterName);
        for (BrokerMaster brokerMaster : brokerAddrs) {
            final SubscriptionGroupConfig subscriptionGroupConfig = defaultMQAdminExt.examineSubscriptionGroupConfig(brokerMaster.getAddr(), consumerGroup);
            brokerSubscribeConfigs.add(new BrokerSubscribeConfig(brokerMaster,subscriptionGroupConfig));
        }
        return brokerSubscribeConfigs;
    }

    /**
     * 创建一个消费组
     * @param connName
     * @param clusterName
     * @param subscriptionGroupConfig
     * @throws Exception
     */
    public void createConsumerGroup(String connName,String clusterName,SubscriptionGroupConfig subscriptionGroupConfig) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final List<BrokerMaster> brokerAddrs = rocketMqClusterService.fetchMastersInCluster(connName, clusterName);
        for (BrokerMaster brokerMaster : brokerAddrs) {
            defaultMQAdminExt.createAndUpdateSubscriptionGroupConfig(brokerMaster.getAddr(),subscriptionGroupConfig);
        }
    }

    /**
     * 删除一个消费组
     * @param connName
     * @param clusterName
     * @param consumerGroup
     * @throws Exception
     */
    public void dropConsumerGroup(String connName,String clusterName,String consumerGroup) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final List<BrokerMaster> brokerAddrs = rocketMqClusterService.fetchMastersInCluster(connName, clusterName);
        for (BrokerMaster brokerMaster : brokerAddrs) {
            defaultMQAdminExt.deleteSubscriptionGroup(brokerMaster.getAddr(),consumerGroup);
        }
    }

    /**
     * 重置消费者消费主题的 offset
     * @param connName
     * @param resetOffsetRequest
     * @return
     * @throws Exception
     */
    public Map<MessageQueue, Long> resetOffset(String connName, ResetOffsetRequest resetOffsetRequest) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final Map<MessageQueue, Long> messageQueueLongMap = defaultMQAdminExt.resetOffsetByTimestamp(resetOffsetRequest.getTopic(), resetOffsetRequest.getConsumerGroup(), resetOffsetRequest.getResetTime(), resetOffsetRequest.isForce());
        return messageQueueLongMap;
    }
}

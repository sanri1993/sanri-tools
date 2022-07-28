package com.sanri.tools.modules.rocketmq.service;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.rocketmq.dtos.RocketMqConnect;
import com.sanri.tools.modules.rocketmq.service.dtos.BrokerMaster;
import com.sanri.tools.modules.rocketmq.service.dtos.BrokerTopicConfig;
import com.sanri.tools.modules.rocketmq.service.dtos.TopicInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.admin.TopicOffset;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.GroupList;
import org.apache.rocketmq.common.protocol.body.TopicList;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;

@Service
@Slf4j
public class RocketMqTopicService {
    @Autowired
    private RocketMqService rocketMqService;
    @Autowired
    private ConnectService connectService;
    @Autowired
    private RocketMqClusterService rocketMqClusterService;

    /**
     * 主题列表查询, 这里只会查询业务主题, 重试主题系统主题, 死信都不在这里查询
     * @param connName
     */
    public Collection<String> normalTopics(@NotBlank String connName) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
        final TopicList systemTopicList = systemTopicList(connName);

        // 去掉重试,系统,死信主题
        final Set<String> collect = topicList.getTopicList().stream().filter(topic -> !topic.startsWith("%RETRY")).collect(Collectors.toSet());
        final Collection<String> subtract = CollectionUtils.subtract(collect, systemTopicList.getTopicList());
        return subtract;
    }

    /**
     * 重试主题列表
     * @param connName
     * @return
     */
    public List<String> retryTopics(@NotBlank String connName) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
        return topicList.getTopicList().stream().filter(topic -> topic.startsWith("%RETRY")).collect(Collectors.toList());
    }

    /**
     * 系统主题查询
     * @param connName
     * @return
     * @throws Exception
     */
    public TopicList systemTopicList(@NotBlank String connName) throws Exception {
        final String text = connectService.loadContent(RocketMqService.MODULE, connName);
        final RocketMqConnect rocketMqConnect = JSON.parseObject(text, RocketMqConnect.class);

        DefaultMQProducer producer = new DefaultMQProducer(MixAll.SELF_TEST_PRODUCER_GROUP);
        producer.setInstanceName(String.valueOf(System.currentTimeMillis()));
        producer.setNamesrvAddr(rocketMqConnect.getNamesrvAddr());
        producer.start();
        try {
            return producer.getDefaultMQProducerImpl().getmQClientFactory().getMQClientAPIImpl().getSystemTopicList(20000L);
        }finally {
            producer.shutdown();
        }
    }

    /**
     * 主题状态信息查询
     * @param connName
     * @param topic
     * @return
     * @throws Exception
     */
    public TopicStatsTable topicStat(@NotBlank String connName, @NotBlank String topic) throws Exception{
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final TopicStatsTable topicStatsTable = defaultMQAdminExt.examineTopicStats(topic);
        return topicStatsTable;
    }

    /**
     * 主题信息
     * @param connName
     * @param topic
     * @return
     * @throws Exception
     */
    public TopicInfo topicInfo(@NotBlank String connName,@NotBlank String topic) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final TopicStatsTable topicStatsTable = defaultMQAdminExt.examineTopicStats(topic);

        final TopicInfo topicInfo = new TopicInfo(topic);

        final Iterator<Map.Entry<MessageQueue, TopicOffset>> iterator = topicStatsTable.getOffsetTable().entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry<MessageQueue, TopicOffset> next = iterator.next();
            final MessageQueue messageQueue = next.getKey();
            final TopicOffset value = next.getValue();

            final TopicInfo.QueueInfo queueInfo = new TopicInfo.QueueInfo(messageQueue.getBrokerName(), messageQueue.getQueueId(), value);
            topicInfo.getQueueInfos().add(queueInfo);
        }
        return topicInfo;
    }

    /**
     * 主题路由信息
     * @param connName
     * @param topic
     * @throws Exception
     * @return
     */
    public TopicRouteData topicRouteInfo(@NotBlank String connName,@NotBlank String topic) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final TopicRouteData topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(topic);
        return topicRouteData;
    }

    /**
     * 查询主题被哪些消费组消费
     * @param connName
     * @param topic
     */
    public Set<String> topicConsumerByWho(@NotBlank String connName,@NotBlank String topic) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final GroupList groupList = defaultMQAdminExt.queryTopicConsumeByWho(topic);
        return groupList.getGroupList();
    }

    /**
     * 主题配置信息
     * @param connName
     * @param clusterName
     * @param topic
     * @throws Exception
     * @return
     */
    public List<BrokerTopicConfig> topicConfig(@NotBlank String connName, String clusterName, @NotBlank String topic) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final List<BrokerMaster> brokerAddrs = rocketMqClusterService.fetchMastersInCluster(connName, clusterName);
        final List<BrokerTopicConfig> brokerTopicConfigs = new ArrayList<>();
        for (BrokerMaster brokerMaster : brokerAddrs) {
            final TopicConfig topicConfig = defaultMQAdminExt.examineTopicConfig(brokerMaster.getAddr(), topic);
            final BrokerTopicConfig brokerTopicConfig = new BrokerTopicConfig(brokerMaster,topicConfig);
            brokerTopicConfigs.add(brokerTopicConfig);
        }
        return brokerTopicConfigs;
    }

    /**
     * 创建一个主题
     * @param connName 连接名
     * @param clusterName 集群名
     * @param topicConfig 主题配置
     */
    public void createTopic(@NotBlank String connName,String clusterName,TopicConfig topicConfig) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final List<BrokerMaster> brokerAddrs = rocketMqClusterService.fetchMastersInCluster(connName, clusterName);
        for (BrokerMaster brokerMaster : brokerAddrs) {
            defaultMQAdminExt.createAndUpdateTopicConfig(brokerMaster.getAddr(),topicConfig);
        }
    }

    /**
     * 删除一个主题
     * @param connName
     * @param clusterName
     * @param topic
     * @throws Exception
     */
    public void dropTopic(@NotBlank String connName,String clusterName,@NotBlank String topic) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);

        final List<BrokerMaster> brokerMasters = rocketMqClusterService.fetchMastersInCluster(connName, clusterName);
        final Set<String> addrs = brokerMasters.stream().map(BrokerMaster::getAddr).collect(Collectors.toSet());
        defaultMQAdminExt.deleteTopicInBroker(addrs,topic);
    }
}

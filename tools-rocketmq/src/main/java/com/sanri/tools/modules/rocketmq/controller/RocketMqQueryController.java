package com.sanri.tools.modules.rocketmq.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotBlank;

import com.sanri.tools.modules.rocketmq.service.dtos.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.common.protocol.body.TopicList;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanri.tools.modules.rocketmq.service.RocketMqClusterService;
import com.sanri.tools.modules.rocketmq.service.RocketMqConsumerService;
import com.sanri.tools.modules.rocketmq.service.RocketMqMessageService;
import com.sanri.tools.modules.rocketmq.service.RocketMqTopicService;

import lombok.extern.slf4j.Slf4j;

/**
 * 主题 ,队列, 消费组, 消息的查询
 */
@RestController
@Slf4j
@RequestMapping("/rocketmq/query")
@Validated
public class RocketMqQueryController {

    @Autowired
    private RocketMqClusterService rocketMqClusterService;
    @Autowired
    private RocketMqTopicService rocketMqTopicService;
    @Autowired
    private RocketMqConsumerService rocketMqConsumerService;
    @Autowired
    private RocketMqMessageService rocketMqMessageService;

    /**
     * 集群信息查询
     * @param connName 连接名
     * @return
     * @throws Exception
     */
    @GetMapping("/clusters")
    public Map<String, RocketMqCluster> clusters(@NotBlank String connName) throws Exception{
        return rocketMqClusterService.clusters(connName);
    }

    /**
     * 主题列表查询
     * @param connName 连接名
     * @param type 主题类型, 路径变量 <br/>
     *        <ul>
     *             <li>normal: 普通主题</li>
     *             <li>retry: 重试主题</li>
     *             <li>system: 系统主题</li>
     *             <li>dlq: 死死主题</li>
     *        </ul>
     * @return
     * @throws Exception
     */
    @GetMapping({"/topics/{type}","/topics"})
    public Collection<String> topics(@NotBlank String connName, @PathVariable(value = "type",required = false) String type) throws Exception {
        if (StringUtils.isBlank(type)) {
            return rocketMqTopicService.normalTopics(connName);
        }
        switch (type){
            case "normal":
                return rocketMqTopicService.normalTopics(connName);
            case "retry":
                return rocketMqTopicService.retryTopics(connName);
            case "system":
                final TopicList systemTopicList = rocketMqTopicService.systemTopicList(connName);
                return systemTopicList.getTopicList();
            case "dlq":
                // 还不知道怎么过滤
                break;
            default:

        }
        return rocketMqTopicService.normalTopics(connName);
    }

    /**
     * 消费组列表查询
     * @param connName 连接名
     * @return
     */
    @GetMapping("/consumerGroups")
    public Set<String> consumerGroups(@NotBlank String connName) throws Exception {
        return rocketMqConsumerService.consumerGroups(connName);
    }

    /**
     * 消费组状态
     * @param connName
     * @param consumerGroup
     * @return
     * @throws Exception
     */
    @GetMapping("/consumerGroup/stat")
    public ConsumeStats consumerStat(@NotBlank String connName, @NotBlank String consumerGroup) throws Exception {
        return rocketMqConsumerService.consumerStat(connName,consumerGroup);
    }

    /**
     * 消费组运行时信息
     * @param connName
     * @param consumerGroup
     * @return
     */
    @GetMapping("/consumerGroup/runningInfo")
    public ConsumerRunningInfo consumerRunningInfo(@NotBlank String connName, @NotBlank String consumerGroup, String clientId, boolean jstack) throws Exception {
        return rocketMqConsumerService.consumerGroupRunningInfo(connName,consumerGroup,clientId,jstack);
    }



    /**
     * 订阅主题列表
     * @param connName
     * @param consumerGroup
     * @return
     */
    @GetMapping("/consumerGroup/subscribes")
    public Set<String> subscribes(@NotBlank String connName, @NotBlank String consumerGroup) throws Exception {
        return rocketMqConsumerService.subscribes(connName, consumerGroup);
    }

    /**
     * 消费组订阅主题信息
     * @param connName
     * @param consumerGroup
     * @return
     * @throws Exception
     */
    @GetMapping("/consumerGroup/subscribeTopics")
    public Collection<ConsumerTopic> subscribeTopics(@NotBlank String connName, @NotBlank String consumerGroup) throws Exception {
        return rocketMqConsumerService.subscribeTopics(connName, consumerGroup);
    }

    /**
     * 消费组的消费模式信息
     * @param connName
     * @param consumerGroup
     * @return
     * @throws Exception
     */
    @GetMapping("/consumerGroup/connInfo")
    public ConsumerConnection consumerGroupConnInfo(@NotBlank String connName, @NotBlank String consumerGroup) throws Exception {
        return rocketMqConsumerService.consumerGroupConnection(connName, consumerGroup);
    }

    /**
     * 消费组配置
     * @param connName
     * @param clusterName
     * @param consumerGroup
     * @return
     * @throws Exception
     */
    @GetMapping("/consumerGroup/config")
    public List<BrokerSubscribeConfig> consumerGroupConfig(@NotBlank String connName, String clusterName, @NotBlank String consumerGroup) throws Exception {
        return rocketMqConsumerService.consumerGroupConfig(connName, clusterName, consumerGroup);
    }

    /**
     * 主题队列信息
     * @param connName 连接名
     * @param topic 主题
     * @return
     */
    @GetMapping("/topic/stat")
    public TopicStatsTable topicStat(@NotBlank String connName, @NotBlank String topic) throws Exception {
        final TopicStatsTable topicStatsTable = rocketMqTopicService.topicStat(connName, topic);
        return topicStatsTable;
    }

    /**
     * 主题队列和 offset 信息
     * @param connName
     * @param topic
     * @return
     */
    @GetMapping("/topic/info")
    public TopicInfo topicInfo(@NotBlank String connName, @NotBlank String topic) throws Exception {
        return rocketMqTopicService.topicInfo(connName, topic);
    }

    /**
     * 主题路由信息
     * @param connName 连接名
     * @param topic 主题
     * @return
     */
    @GetMapping("/topic/route")
    public TopicRouteData topicRoute(@NotBlank String connName, @NotBlank String topic) throws Exception {
        return rocketMqTopicService.topicRouteInfo(connName, topic);
    }

    /**
     * 主题配置, 好像 rocketmq 每个 broker 可以配置不一样, 对于一个主题
     * @param connName
     * @param topic
     * @return
     */
    @GetMapping("/topic/config")
    public List<BrokerTopicConfig> topicConfigs(@NotBlank String connName,String clusterName,@NotBlank String topic) throws Exception {
        return rocketMqTopicService.topicConfig(connName,clusterName,topic);
    }

    /**
     * 查询主题被哪些消费组消费
     * @param connName 连接名
     * @param topic 主题
     * @return
     */
    @GetMapping("/topic/consumerByWho")
    public Set<String> topicConsumerByWho(@NotBlank String connName,@NotBlank String topic) throws Exception {
        return rocketMqTopicService.topicConsumerByWho(connName, topic);
    }

    /**
     * 查询消息
     * @param connName
     * @param messageQueryParam
     * @return
     */
    @GetMapping("/topic/queryMessageByKey")
    public List<MessageView> queryMessageByKey(@NotBlank String connName, MessageQueryParam messageQueryParam) throws Exception {
        final List<MessageView> messageExts = rocketMqMessageService.queryMessageByKey(connName, messageQueryParam);
        return messageExts;
    }

    /**
     * 消费主题消息
     * @param connName
     * @param dataConsumerParam
     * @return
     */
    @GetMapping("/topic/consumerMessage")
    public List<MessageView> consumerMessage(@NotBlank String connName, OffsetDataConsumerParam dataConsumerParam) throws Exception {
        final List<MessageView> messageExts = rocketMqMessageService.consumerMessage(connName, dataConsumerParam);
        return messageExts;
    }

    /**
     * 根据时间来消费数据
     * @param connName
     * @param timestampDataConsumerParam
     * @return
     * @throws Exception
     */
    @GetMapping("/topic/consumerMessageByTime")
    public List<MessageView> consumerMessageByTime(@NotBlank String connName, TimestampDataConsumerParam timestampDataConsumerParam) throws Exception {
        final List<MessageView> messageExts = rocketMqMessageService.consumerMessageByTime(connName, timestampDataConsumerParam);
        return messageExts;
    }

    /**
     * 消息轨迹查询
     * @param connName 连接名
     * @param topic 主题
     * @param msgId 消息ID
     * @return
     * @throws Exception
     */
    @GetMapping("/messageTrace")
    public List<MessageTrack> messageTrace(@NotBlank String connName, String topic, String msgId) throws Exception {
        return rocketMqMessageService.messageTrace(connName,topic,msgId);
    }
}

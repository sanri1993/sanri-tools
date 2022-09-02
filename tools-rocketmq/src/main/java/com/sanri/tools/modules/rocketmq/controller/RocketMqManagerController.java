package com.sanri.tools.modules.rocketmq.controller;

import com.sanri.tools.modules.rocketmq.service.RocketMqConsumerService;
import com.sanri.tools.modules.rocketmq.service.RocketMqProducerService;
import com.sanri.tools.modules.rocketmq.service.RocketMqTopicService;
import com.sanri.tools.modules.rocketmq.service.dtos.ResetOffsetRequest;
import com.sanri.tools.modules.rocketmq.service.dtos.SendTopicMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.subscription.SubscriptionGroupConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 主题, 消费组的管理
 */
@RestController
@Slf4j
@RequestMapping("/rocketmq/manager")
public class RocketMqManagerController {
    @Autowired
    private RocketMqTopicService rocketMqTopicService;
    @Autowired
    private RocketMqProducerService rocketMqProducerService;
    @Autowired
    private RocketMqConsumerService rocketMqConsumerService;

    /**
     * 创建一个主题
     * @param connName
     * @param clusterName
     * @param topicConfig
     */
    @PostMapping("/topic/createTopic")
    public void createTopic(String connName,String clusterName, @RequestBody TopicConfig topicConfig) throws Exception {
        rocketMqTopicService.createTopic(connName, clusterName, topicConfig);
    }

    /**
     * 删除一个主题
     * @param connName
     * @param clusterName
     * @param topicName
     */
    @PostMapping("/topic/dropTopic")
    public void dropTopic(String connName,String clusterName,String topicName) throws Exception {
        rocketMqTopicService.dropTopic(connName, clusterName, topicName);
    }

    /**
     * 给主题发送消息
     * @param connName
     * @param sendTopicMessageRequest
     * @return
     * @throws Exception
     */
    @PostMapping("/topic/sendMessage/{connName}")
    public SendResult sendMessage(@PathVariable("connName") String connName, @RequestBody SendTopicMessageRequest sendTopicMessageRequest) throws Exception {
        return rocketMqProducerService.sendMessage(connName, sendTopicMessageRequest);
    }

    /**
     * @param connName
     * @param resetOffsetRequest
     * @ignore
     * @return
     */
    @PostMapping("/consumerGroup/resetOffset/{connName}")
    public Map<MessageQueue, Long> resetOffset(@PathVariable("connName") String connName, @RequestBody ResetOffsetRequest resetOffsetRequest) throws Exception {
        return rocketMqConsumerService.resetOffset(connName, resetOffsetRequest);
    }

    /**
     * 创建消费组
     * @param connName
     * @param clusterName
     * @param subscriptionGroupConfig
     * @throws Exception
     */
    @PostMapping("/consumerGroup/createGroup")
    public void createConsumerGroup(String connName,String clusterName, @RequestBody SubscriptionGroupConfig subscriptionGroupConfig) throws Exception {
        rocketMqConsumerService.createConsumerGroup(connName, clusterName, subscriptionGroupConfig);
    }

    /**
     * 删除消费组
     * @param connName
     * @param clusterName
     * @param consumerGroup
     * @throws Exception
     */
    @PostMapping("/consumerGroup/dropGroup")
    public void dropConsumerGroup(String connName,String clusterName, String consumerGroup) throws Exception {
        rocketMqConsumerService.dropConsumerGroup(connName, clusterName, consumerGroup);
    }
}

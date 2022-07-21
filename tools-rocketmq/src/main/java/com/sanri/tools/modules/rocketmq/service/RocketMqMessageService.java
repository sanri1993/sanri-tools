package com.sanri.tools.modules.rocketmq.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sanri.tools.modules.rocketmq.service.dtos.TopicMessageResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.rocketmq.dtos.RocketMqConnect;
import com.sanri.tools.modules.rocketmq.service.dtos.QueueMessageQueryParam;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RocketMqMessageService {
    @Autowired
    private RocketMqService rocketMqService;

    private final static int QUERY_MESSAGE_MAX_NUM = 64;

    /**
     * 分页查询消息, 指定队列名
     * @param connName 连接名
     * @param queueMessageQueryParam 查询参数
     */
    public TopicMessageResponse queryTopicQueueMessageByPage(String connName, QueueMessageQueryParam queueMessageQueryParam) throws Exception{
        final RocketMqConnect rocketMqConnect = rocketMqService.loadConnect(connName);
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(MixAll.SELF_TEST_CONSUMER_GROUP);
        try {
            consumer.setNamesrvAddr(rocketMqConnect.getNamesrvAddr());
            consumer.start();

            final int queueId = queueMessageQueryParam.getQueueId();

            final Set<MessageQueue> messageQueues = consumer.fetchSubscribeMessageQueues(queueMessageQueryParam.getTopic());
            final Optional<MessageQueue> messageQueueOptional = messageQueues.stream().filter(q -> queueId == q.getQueueId()).findFirst();
            final MessageQueue messageQueue = messageQueueOptional.orElseThrow(() -> new ToolException("未找到队列:" + queueId));

            long offset = queueMessageQueryParam.getOffset();
            if (offset <= 0) {
                // 如果是第一次查询, 需要取 offset 为最小 offset
                offset = consumer.minOffset(messageQueue);
            }

            final long maxOffset = consumer.maxOffset(messageQueue);
            if (offset >= maxOffset) {
                log.warn("offset 超过最大 offset [{}>{}]", offset, maxOffset);
                throw new ToolException("没有更多消息了");
            }

            final PullResult pullResult = consumer.pull(messageQueue, "*", offset, queueMessageQueryParam.getPageSize());
            if (pullResult.getPullStatus() != PullStatus.FOUND) {
                log.warn("没有更多消息了[{}]", offset);
                throw new ToolException("没有更多消息了");
            }

            final long nextBeginOffset = pullResult.getNextBeginOffset();
            final List<MessageExt> msgFoundList = pullResult.getMsgFoundList();
            return new TopicMessageResponse(queueId, nextBeginOffset, pullResult.getMinOffset(), pullResult.getMaxOffset(), msgFoundList);
        }finally {
            consumer.shutdown();
        }
    }

    /**
     * 分页查询消息, 不指定队列名
     * @param connName 连接名
     * @param queueMessageQueryParam 查询参数
     * @throws Exception
     */
    public TopicMessageResponse queryTopicMessageByPage(String connName, QueueMessageQueryParam queueMessageQueryParam) throws Exception{
        final RocketMqConnect rocketMqConnect = rocketMqService.loadConnect(connName);
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(MixAll.SELF_TEST_CONSUMER_GROUP);
        try {
            consumer.setNamesrvAddr(rocketMqConnect.getNamesrvAddr());
            consumer.start();

            final Set<MessageQueue> messageQueues = consumer.fetchSubscribeMessageQueues(queueMessageQueryParam.getTopic());
            final List<MessageQueue> messageQueueList = messageQueues.stream().sorted((a, b) -> a.getQueueId() - b.getQueueId()).collect(Collectors.toList());

            List<MessageExt> allFoundMsgList = new ArrayList<>();
            final TopicMessageResponse topicMessageResponse = new TopicMessageResponse();

            boolean firstRead = true;
            for (MessageQueue messageQueue : messageQueueList) {
                final int queueId = messageQueue.getQueueId();
                if (queueMessageQueryParam.getQueueId() > 0 && !String.valueOf(queueId).equals(queueMessageQueryParam.getQueueId()) && firstRead){
                    continue;
                }
                // 找到当前队列, 开始读取消息
                final long minOffset = consumer.minOffset(messageQueue);
                final long maxOffset = consumer.maxOffset(messageQueue);

                long offset = (firstRead && queueMessageQueryParam.getOffset() > 0 ) ? queueMessageQueryParam.getOffset() : minOffset;
                final PullResult pullResult = consumer.pull(messageQueue, "*", offset, queueMessageQueryParam.getPageSize() - allFoundMsgList.size());
                if (pullResult.getPullStatus() != PullStatus.FOUND){
                    // 没有找到足够消息数量时继续下一次读取
                    continue;
                }
                final List<MessageExt> msgFoundList = pullResult.getMsgFoundList();
                allFoundMsgList.addAll(msgFoundList);
                if (allFoundMsgList.size() >= queueMessageQueryParam.getPageSize()){
                    topicMessageResponse.setQueueId(queueId);
                    topicMessageResponse.setBeginOffset(pullResult.getNextBeginOffset());
                    topicMessageResponse.setMinOffset(pullResult.getMinOffset());
                    topicMessageResponse.setMaxOffset(pullResult.getMaxOffset());
                    topicMessageResponse.setBeginOffset(pullResult.getNextBeginOffset());
                    break;
                }
                firstRead = false;
            }

            topicMessageResponse.setMsgFoundList(allFoundMsgList);
            return topicMessageResponse;
        }finally {
            try {
                if (consumer != null){
                    consumer.shutdown();
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }


    /**
     * 查询消息轨迹
     * @param connName 连接名
     * @param topic 主题
     * @param msgId 消息Id
     * @return
     */
    public List<MessageTrack> messageTrace(String connName, String topic, String msgId) throws Exception {
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);

        final MessageExt messageExt = defaultMQAdminExt.viewMessage(topic, msgId);
        final List<MessageTrack> messageTracks = defaultMQAdminExt.messageTrackDetail(messageExt);
        return messageTracks;
    }
}

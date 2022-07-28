package com.sanri.tools.modules.rocketmq.service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.rocketmq.service.dtos.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sanri.tools.modules.rocketmq.dtos.RocketMqConnect;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RocketMqMessageService {
	@Autowired
	private RocketMqService rocketMqService;

	/**
	 * 查询消息列表
	 * @param connName
	 * @param messageQueryParam
	 * @return
	 * @throws Exception
	 */
	public List<MessageView> queryMessageByKey(String connName, MessageQueryParam messageQueryParam) throws Exception{
		final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
		final QueryResult queryResult = defaultMQAdminExt.queryMessage(messageQueryParam.getTopic(), messageQueryParam.getKey(), messageQueryParam.getMaxNum(), messageQueryParam.getBegin().getTime(), messageQueryParam.getEnd().getTime());
		final List<MessageExt> messageList = queryResult.getMessageList();
		final List<MessageView> messageViews = Lists.transform(messageList, messageExt -> {
			final MessageView messageView = new MessageView();
			BeanUtils.copyProperties(messageExt, messageView);
			final byte[] body = messageExt.getBody();
			messageView.setBody(new String(body, StandardCharsets.UTF_8));
			return messageView;
		});
		return messageViews;
	}

	/**
	 * 根据时间查询消息
	 * @param connName
	 * @param timestampDataConsumerParam
	 * @return
	 */
	public List<MessageView> consumerMessageByTime(String connName, TimestampDataConsumerParam timestampDataConsumerParam) throws Exception{
		final RocketMqConnect rocketMqConnect = rocketMqService.loadConnect(connName);
		DefaultLitePullConsumer consumer = new DefaultLitePullConsumer(MixAll.SELF_TEST_CONSUMER_GROUP);
		consumer.setNamesrvAddr(rocketMqConnect.getNamesrvAddr());
		consumer.setAutoCommit(false);
		try{
			consumer.start();

			final Collection<MessageQueue> messageQueues = consumer.fetchMessageQueues(timestampDataConsumerParam.getTopic());
			final List<MessageQueue> assignMessageQueue = messageQueues.stream().filter(messageQueue -> messageQueue.getQueueId() == timestampDataConsumerParam.getQueueId()).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(assignMessageQueue)){
				throw new ToolException("没有这个队列:"+timestampDataConsumerParam.getQueueId());
			}
			final MessageQueue messageQueue = assignMessageQueue.get(0);
			consumer.assign(Arrays.asList(messageQueue));
			final Long offsetForTimestamp = consumer.offsetForTimestamp(messageQueue, timestampDataConsumerParam.getTime().getTime());
			consumer.seek(messageQueue, offsetForTimestamp);

			return loadData(timestampDataConsumerParam,consumer);
		}finally {
			consumer.shutdown();
		}
	}

	/**
	 * 方法耗时 30s
	 * @param connName
	 * @param dataConsumerParam
	 * @throws Exception
	 * @return
	 */
	public List<MessageView> consumerMessage(String connName, OffsetDataConsumerParam dataConsumerParam) throws Exception {
		final RocketMqConnect rocketMqConnect = rocketMqService.loadConnect(connName);
		DefaultLitePullConsumer consumer = new DefaultLitePullConsumer(MixAll.SELF_TEST_CONSUMER_GROUP);
		consumer.setNamesrvAddr(rocketMqConnect.getNamesrvAddr());
		consumer.setAutoCommit(false);
		try{
			consumer.start();

			final Collection<MessageQueue> messageQueues = consumer.fetchMessageQueues(dataConsumerParam.getTopic());
			final List<MessageQueue> assignMessageQueue = messageQueues.stream().filter(messageQueue -> messageQueue.getQueueId() == dataConsumerParam.getQueueId()).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(assignMessageQueue)){
				throw new ToolException("没有这个队列:"+dataConsumerParam.getQueueId());
			}
			final MessageQueue messageQueue = assignMessageQueue.get(0);
			consumer.assign(Arrays.asList(messageQueue));
			consumer.seek(messageQueue, dataConsumerParam.getOffset());

			return loadData(dataConsumerParam, consumer);
		}finally {
			consumer.shutdown();
		}
	}

	public List<MessageView> loadData(BaseDataConsumerParam dataConsumerParam, DefaultLitePullConsumer consumer) {
		final List<MessageExt> messageExts = new ArrayList<>();
		long startConsumerTime = System.currentTimeMillis();
		int retryCount = 0;
		while (messageExts.size() < dataConsumerParam.getLimit() && (System.currentTimeMillis() - startConsumerTime) < 30000 && retryCount++ < 5 ){
			final List<MessageExt> poll = consumer.poll();
			messageExts.addAll(poll);
		}
		log.info("在尝试{}次后, 拿到数据量: {}",retryCount,messageExts.size());

		final List<MessageView> messageViews = Lists.transform(messageExts, messageExt -> {
			final MessageView messageView = new MessageView();
			BeanUtils.copyProperties(messageExt, messageView);
			final byte[] body = messageExt.getBody();
			messageView.setBody(new String(body, StandardCharsets.UTF_8));
			return messageView;
		});
		return messageViews;
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

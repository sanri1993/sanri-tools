package com.sanri.tools.modules.rocketmq.service;

import com.google.common.base.Throwables;
import com.sanri.tools.modules.rocketmq.dtos.RocketMqConnect;
import com.sanri.tools.modules.rocketmq.service.dtos.SendTopicMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RocketMqProducerService {
    @Autowired
    private RocketMqService rocketMqService;

    /**
     * 推送消息给主题
     * @param sendMessageDto
     * @param connName
     * @throws Exception
     * @return
     */
    public SendResult sendMessage(String connName, SendTopicMessageRequest sendTopicMessageRequest) throws Exception{
        final RocketMqConnect rocketMqConnect = rocketMqService.loadConnect(connName);

        DefaultMQProducer producer = new DefaultMQProducer(MixAll.SELF_TEST_PRODUCER_GROUP);
        producer.setInstanceName(String.valueOf(System.currentTimeMillis()));
        producer.setNamesrvAddr(rocketMqConnect.getNamesrvAddr());
        try {
            producer.start();
            Message msg = new Message(sendTopicMessageRequest.getTopic(),
                    sendTopicMessageRequest.getTag(),
                    sendTopicMessageRequest.getKey(),
                    sendTopicMessageRequest.getMessageBody().getBytes()
            );
            return producer.send(msg);
        }
        finally {
            producer.shutdown();
        }
    }
}

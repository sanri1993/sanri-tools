package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.ArrayList;
import java.util.List;

@Data
public class TopicMessageResponse {
    /**
     * 当前查询的是哪个 queue
     */
    private int queueId;

    /**
     * 下次查询的起始 offset
     */
    private long beginOffset;

    /**
     * 最小 offset
     */
    private long minOffset;
    /**
     * 最大 offset
     */
    private long maxOffset;

    /**
     * 消息列表
     */
    private List<MessageExt> msgFoundList = new ArrayList<>();

    public TopicMessageResponse() {
    }


    public TopicMessageResponse(int queueId, long beginOffset, long minOffset, long maxOffset, List<MessageExt> msgFoundList) {
        this.queueId = queueId;
        this.beginOffset = beginOffset;
        this.minOffset = minOffset;
        this.maxOffset = maxOffset;
        this.msgFoundList = msgFoundList;
    }
}

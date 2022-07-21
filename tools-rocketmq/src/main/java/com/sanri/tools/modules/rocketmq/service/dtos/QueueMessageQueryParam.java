package com.sanri.tools.modules.rocketmq.service.dtos;

import com.sanri.tools.modules.core.dtos.param.PageParam;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Data
public class QueueMessageQueryParam {
    @NotBlank
    private String topic;

    private int queueId = -1;
    private String key;

    /**
     * 由前端来维护 offset
     */
    private long offset = -1;

    private int pageSize;
}

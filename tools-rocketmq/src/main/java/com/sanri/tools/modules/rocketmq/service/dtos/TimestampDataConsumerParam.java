package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class TimestampDataConsumerParam extends BaseDataConsumerParam{
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;
}

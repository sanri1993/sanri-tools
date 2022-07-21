package com.sanri.tools.modules.rocketmq.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RocketMqDashboardService {
    @Autowired
    private RocketMqService rocketMqService;
}

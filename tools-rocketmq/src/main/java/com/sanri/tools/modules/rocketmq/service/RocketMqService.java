package com.sanri.tools.modules.rocketmq.service;

import com.alibaba.fastjson.JSON;
import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.rocketmq.dtos.RocketMqConnect;
import io.netty.channel.DefaultChannelId;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主服务, 主要用于管理 mq 实例信息
 */
@Service
@Slf4j
public class RocketMqService {
    /**
     * connName => mq 管理端实例
     */
    private Map<String, DefaultMQAdminExt> mqAdminExtMap = new ConcurrentHashMap<>();
    /**
     * connName => mq 生产者实例
     */
    private Map<String, DefaultMQProducer> mqProducerMap = new ConcurrentHashMap<>();

    public static final String MODULE = "rocketmq";

    @Autowired
    private ConnectService connectService;

    static {
        // 提前执行静态代码, 避免超时
        DefaultChannelId.newInstance();
    }

    /**
     * 创建或者获取一个 rocketMq 管理实例
     * @param connName
     * @return
     * @throws IOException
     */
    public DefaultMQAdminExt loadRocketMqAdmin(String connName) throws Exception {
        if (mqAdminExtMap.containsKey(connName)){
            return mqAdminExtMap.get(connName);
        }

        final String text = connectService.loadContent(MODULE, connName);
        final RocketMqConnect rocketMqConnect = JSON.parseObject(text, RocketMqConnect.class);

        final DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setNamesrvAddr(rocketMqConnect.getNamesrvAddr());
        defaultMQAdminExt.start();
        mqAdminExtMap.put(connName,defaultMQAdminExt);
        return defaultMQAdminExt;
    }

    /**
     * 获取 rocketMq 连接
     * @param connName
     * @return
     * @throws IOException
     */
    public RocketMqConnect loadConnect(String connName) throws IOException {
        final String text = connectService.loadContent(MODULE, connName);
        return JSON.parseObject(text, RocketMqConnect.class);
    }
}

package com.sanri.tools.modules.rocketmq.service;

import com.alibaba.fastjson.JSON;
import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectInput;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectOutput;
import com.sanri.tools.modules.core.service.connect.events.SecurityConnectEvent;
import com.sanri.tools.modules.rocketmq.dtos.RocketMqConnect;
import io.netty.channel.DefaultChannelId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主服务, 主要用于管理 mq 实例信息
 */
@Service
@Slf4j
public class RocketMqService implements InitializingBean, ApplicationListener<SecurityConnectEvent> {
    /**
     * connName => mq 管理端实例
     */
    private Map<String, DefaultMQAdminExt> mqAdminExtMap = new ConcurrentHashMap<>();

    public static final String MODULE = "rocketmq";

    @Autowired
    private ConnectService connectService;


    /**
     * 创建或者获取一个 rocketMq 管理实例
     * @param connName
     * @return
     * @throws IOException
     */
    public synchronized DefaultMQAdminExt loadRocketMqAdmin(String connName) throws Exception {
        if (mqAdminExtMap.containsKey(connName)){
            return mqAdminExtMap.get(connName);
        }

        final String text = connectService.loadContent(MODULE, connName);
        final RocketMqConnect rocketMqConnect = JSON.parseObject(text, RocketMqConnect.class);

        DefaultMQAdminExt defaultMQAdminExt = null;
        if (StringUtils.isNotBlank(rocketMqConnect.getAccessKey())){
            AclClientRPCHook rpcHook = new AclClientRPCHook(new SessionCredentials(rocketMqConnect.getAccessKey(), rocketMqConnect.getSecretKey()));
            defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
            defaultMQAdminExt.setUseTLS(rocketMqConnect.getUseTLS() == null ? false : rocketMqConnect.getUseTLS());
        }else {
            defaultMQAdminExt = new DefaultMQAdminExt();
        }
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

    @Override
    public void afterPropertiesSet() throws Exception {
        // 提前执行静态代码, 避免超时
        new Thread(){
            @Override
            public void run() {
                log.info("初始化耗时方法");
                long startTime = System.currentTimeMillis();
                DefaultChannelId.newInstance();
                log.info("初始化耗时方法结束, 耗时:{} ms",(System.currentTimeMillis() - startTime));
            }
        }.start();
    }

    @Override
    public void onApplicationEvent(SecurityConnectEvent event) {
        ConnectOutput connectOutput = (ConnectOutput) event.getSource();
        final ConnectInput connectInput = connectOutput.getConnectInput();
        if (MODULE.equals(connectInput.getModule())){
            final DefaultMQAdminExt mqAdminExt = mqAdminExtMap.remove(connectInput.getBaseName());
            if (mqAdminExt != null){
                try {
                    mqAdminExt.shutdown();
                }catch (Exception e){
                    // ignore
                }
            }
            log.info("[{}]模块[{}]配置变更,将移除存储的元数据信息", MODULE,connectInput.getBaseName());
        }
    }
}

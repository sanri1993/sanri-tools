package com.sanri.tools.maven.service;

import com.sanri.tools.modules.core.service.file.Tailf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author sanri
 * maven  goal 执行日志推送
 *
 * 实现类似  tail -f 的能力
 */
@RestController
@Slf4j
public class GoalsExecLogPushEndpoint {
    @Autowired
    private GoalExecuteLogManager goalExecuteLogManager;
    @Autowired
    private Tailf tailf;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 订阅执行日志推送
     * @param logPath
     * @param clientId
     */
    public void subscribeExecLog(String logPath,String clientId) throws IOException, InterruptedException {
        // 获取文件
        final File logFile = goalExecuteLogManager.logFile(logPath);

        // 实时推送文件信息
        final Tailf.InnerTail innerTail = tailf.startTail(logFile);
        innerTail.register(new Tailf.LineUpdateListener() {
            private String destination = "/topic/maven/logs/"+logFile.getName()+"/"+clientId;

            @Override
            public void update(String line) {
                simpMessagingTemplate.convertAndSend(destination,line);
            }
        });
    }
}

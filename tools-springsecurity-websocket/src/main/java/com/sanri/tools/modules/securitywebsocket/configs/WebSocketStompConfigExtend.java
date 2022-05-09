package com.sanri.tools.modules.securitywebsocket.configs;

import com.sanri.tools.modules.security.configs.SpringAsyncSecurityConfig;
import com.sanri.tools.modules.websocket.configs.WebSocketStompConfig;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfigExtend implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private AuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);
        taskExecutor.setAllowCoreThreadTimeOut(true);
        taskExecutor.setTaskDecorator(new SpringAsyncSecurityConfig.AsyncTaskDecorator());
        registration.taskExecutor(taskExecutor);
    }


}

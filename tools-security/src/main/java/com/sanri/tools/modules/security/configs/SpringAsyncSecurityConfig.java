package com.sanri.tools.modules.security.configs;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 添加用户信息的异步线程配置,覆盖默认异步线程配置
 */
@Configuration
public class SpringAsyncSecurityConfig {
    @Bean(name = "asyncExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setThreadGroupName("MyCustomExecutor");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setBeanName("asyncExecutor");
        executor.setTaskDecorator(new AsyncTaskDecorator());
        return executor;
    }

    /**
     * 对于异步任务时, 同样也能获取到 TraceId 和权限信息
     * spring 的异步任务 @Async
     */
    public static class AsyncTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            try {
                RequestAttributes context = RequestContextHolder.currentRequestAttributes();
                Map<String,String> previous = MDC.getCopyOfContextMap();
                final SecurityContext securityContext = SecurityContextHolder.getContext();
                return () -> {
                    try {
                        RequestContextHolder.setRequestAttributes(context);

                        SecurityContextHolder.setContext(securityContext);

                        MDC.setContextMap(previous);

                        runnable.run();
                    } finally {
                        RequestContextHolder.resetRequestAttributes();

                        SecurityContextHolder.clearContext();

                        MDC.clear();
                    }
                };
            } catch (IllegalStateException e) {
                return runnable;
            }
        }
    }
}

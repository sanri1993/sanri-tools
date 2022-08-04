package com.sanri.tools.modules.core.configs;

import com.sanri.tools.modules.core.service.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 项目中公共线程池配置
 */
@Configuration
@Slf4j
public class ThreadPoolsConfig {

    /**
     * 快速任务线程池
     * 场景: 任务多且执行快, 不能排队的情况; 当容纳不了时, 直接报错
     * @return
     */
    @Bean
    public ThreadPoolExecutor publicFastThreadPool(){
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
        final ThreadFactory threadFactory = new NamedThreadFactory("fastThreadPool");
        final RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                throw new RejectedExecutionException();
            }
        };
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5,100,1, TimeUnit.MINUTES,queue,threadFactory,rejectedExecutionHandler);
        return executor;
    }

    /**
     * 慢速任务线程池
     * 场景: 任务不需要时效性, 可慢慢排队执行, 当任务堵塞时慢慢排队执行
     * @return
     */
    @Bean
    public ThreadPoolExecutor publicSlowThreadPool(){
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1000);
        final ThreadFactory threadFactory = new NamedThreadFactory("slowThreadPool");
        final RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                log.warn("当前任务数量, 超过线程池限制, 将在原来线程执行");
                r.run();
            }
        };
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1,10,10, TimeUnit.SECONDS,queue,threadFactory,rejectedExecutionHandler);
        return executor;
    }

    @Bean
    public ThreadPoolExecutor localTaskThreadPool(){
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1000);
        final ThreadFactory threadFactory = new NamedThreadFactory("localTaskThreadPool");
        final RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                log.warn("当前任务数量, 超过线程池限制, 将在原来线程执行");
                r.run();
            }
        };
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1,10,10, TimeUnit.SECONDS,queue,threadFactory,rejectedExecutionHandler);
        return executor;
    }
}

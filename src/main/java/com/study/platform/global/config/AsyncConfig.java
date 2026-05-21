package com.study.platform.global.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Map;
import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("notification-");
        executor.setVirtualThreads(true);
        executor.setTaskDecorator(mdcTaskDecorator());
        return executor;
    }

    private TaskDecorator mdcTaskDecorator() {
        return task -> {
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (mdcContext != null) {
                        MDC.setContextMap(mdcContext);
                    } else {
                        MDC.clear();
                    }
                    task.run();
                } finally {
                    MDC.clear();
                }
            };
        };
    }
}
package com.study.platform.global.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Map;
import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        return task -> Thread.ofVirtual()
                .name("notification-", 0)
                .start(mdcTaskDecorator().decorate(task));
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
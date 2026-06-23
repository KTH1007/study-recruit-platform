package com.study.platform.global.config

import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskDecorator
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.Executor

@EnableAsync
@Configuration
class AsyncConfig {

    @Bean(name = ["notificationExecutor"])
    fun notificationExecutor(): Executor {
        val executor = SimpleAsyncTaskExecutor("notification-")
        executor.setVirtualThreads(true)
        executor.setTaskDecorator(mdcTaskDecorator())
        return executor
    }

    private fun mdcTaskDecorator(): TaskDecorator = TaskDecorator { task ->
        val mdcContext = MDC.getCopyOfContextMap()
        Runnable {
            try {
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext)
                } else {
                    MDC.clear()
                }
                task.run()
            } finally {
                MDC.clear()
            }
        }
    }
}

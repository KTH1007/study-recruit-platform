package com.study.platform.global.config

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskDecorator
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import java.lang.reflect.Method
import java.util.concurrent.Executor

@EnableAsync
@Configuration
class AsyncConfig : AsyncConfigurer {

    private val log = LoggerFactory.getLogger(AsyncConfig::class.java)

    companion object {
        private const val CONCURRENCY_LIMIT = 50
    }

    @Bean(name = ["notificationExecutor"])
    fun notificationExecutor(): Executor {
        val executor = SimpleAsyncTaskExecutor("notification-")
        executor.setVirtualThreads(true)
        executor.concurrencyLimit = CONCURRENCY_LIMIT
        executor.setTaskDecorator(mdcTaskDecorator())
        return executor
    }

    override fun getAsyncExecutor(): Executor = notificationExecutor()

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler =
        AsyncUncaughtExceptionHandler { throwable, method, params -> logUncaughtException(throwable, method, params) }

    private fun logUncaughtException(throwable: Throwable, method: Method, params: Array<out Any?>) {
        log.error(
            "비동기 처리 중 처리되지 않은 예외 발생 - method: {}.{}, params: {}",
            method.declaringClass.simpleName, method.name, params.contentToString(), throwable
        )
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

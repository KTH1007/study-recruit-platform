package com.study.platform.global.config

import com.study.platform.global.constant.KafkaConstants
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.TopicPartition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries

@Configuration
class KafkaConfig {

    @Bean
    fun notificationTopic(): NewTopic =
        TopicBuilder.name(KafkaConstants.NOTIFICATION_TOPIC)
            .partitions(2)
            .replicas(1)
            .build()

    @Bean
    fun postSyncTopic(): NewTopic =
        TopicBuilder.name(KafkaConstants.POST_SYNC_TOPIC)
            .partitions(2)
            .replicas(1)
            .build()

    @Bean
    fun postSyncDltTopic(): NewTopic =
        TopicBuilder.name(KafkaConstants.POST_SYNC_DLT_TOPIC)
            .partitions(2)
            .replicas(1)
            .build()

    @Bean
    fun notificationDltTopic(): NewTopic =
        TopicBuilder.name(KafkaConstants.NOTIFICATION_DLT_TOPIC)
            .partitions(2)
            .replicas(1)
            .build()

    // Spring Kafka가 자동으로 원본토픽명.DLT로 라우팅
    @Bean
    fun errorHandler(kafkaTemplate: KafkaTemplate<String, String>): DefaultErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { record, _ ->
            if (record.topic() == KafkaConstants.POST_SYNC_DLT_TOPIC
                || record.topic() == KafkaConstants.NOTIFICATION_DLT_TOPIC
            ) {
                null // DLT 토픽 자체 실패는 DLT로 재발행하지 않음
            } else {
                TopicPartition(record.topic() + ".DLT", -1)
            }
        }

        val backOff = ExponentialBackOffWithMaxRetries(3)
        backOff.initialInterval = 1_000L
        backOff.multiplier = 2.0

        return DefaultErrorHandler(recoverer, backOff)
    }
}

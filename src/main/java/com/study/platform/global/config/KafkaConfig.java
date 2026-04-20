package com.study.platform.global.config;

import com.study.platform.global.constant.KafkaConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(KafkaConstants.NOTIFICATION_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic postSyncTopic() {
        return TopicBuilder.name(KafkaConstants.POST_SYNC_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic postSyncDltTopic() {
        return TopicBuilder.name(KafkaConstants.POST_SYNC_DLT_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationDltTopic() {
        return TopicBuilder.name(KafkaConstants.NOTIFICATION_DLT_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }

    // Spring Kafka가 자동으로 원본토픽명.DLT로 라우팅
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> {
                    if (record.topic().equals(KafkaConstants.POST_SYNC_DLT_TOPIC)
                            || record.topic().equals(KafkaConstants.NOTIFICATION_DLT_TOPIC)) {
                        return null; // DLT 토픽 자체 실패는 DLT로 재발행하지 않음
                    }
                    return new TopicPartition(record.topic() + ".DLT", -1);
                });

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2.0);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}

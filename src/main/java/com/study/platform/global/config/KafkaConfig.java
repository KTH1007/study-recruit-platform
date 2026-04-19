package com.study.platform.global.config;

import com.study.platform.global.constant.KafkaConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

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
}

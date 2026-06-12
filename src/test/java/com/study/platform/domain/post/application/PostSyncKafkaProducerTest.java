package com.study.platform.domain.post.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.global.outbox.application.OutboxEventService;
import com.study.platform.support.fake.FakeKafkaMessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostSyncKafkaProducerTest {

    @Mock
    private OutboxEventService outboxEventService;

    private FakeKafkaMessagePublisher kafkaPublisher;
    private PostSyncKafkaProducer postSyncKafkaProducer;
    private PostSyncEvent event;

    @BeforeEach
    void setUp() {
        kafkaPublisher = new FakeKafkaMessagePublisher();
        postSyncKafkaProducer = new PostSyncKafkaProducer(kafkaPublisher, new ObjectMapper(), outboxEventService);
        event = new PostSyncEvent(UUID.randomUUID(), PostSyncOperationType.UPSERT, 1L, 0);
    }

    @Test
    void handle_Kafka_발행_성공_markSent_호출() {
        // when
        postSyncKafkaProducer.handle(event);

        // then
        assertThat(kafkaPublisher.getPublishedTopics()).isNotEmpty();
        then(outboxEventService).should().markSent(eq(1L));
    }

    @Test
    void handle_Kafka_발행_실패_markSent_미호출() {
        // given
        kafkaPublisher.willFail();

        // when
        postSyncKafkaProducer.handle(event);

        // then
        then(outboxEventService).should(never()).markSent(anyLong());
    }
}

package com.study.platform.domain.post.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.outbox.application.OutboxEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostSyncKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private PostSyncKafkaProducer postSyncKafkaProducer;

    private PostSyncEvent event;

    @BeforeEach
    void setUp() {
        event = new PostSyncEvent(UUID.randomUUID(), PostSyncOperationType.UPSERT, 0);
    }

    @Test
    void handle_Kafka_발행_성공_markSent_호출() {
        // given
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.complete(mock(SendResult.class));
        given(objectMapper.writeValueAsString(any())).willReturn("{}");
        given(kafkaTemplate.send(anyString(), anyString(), anyString())).willReturn(future);

        // when
        postSyncKafkaProducer.handle(event);

        // then
        then(outboxEventService).should().markSent(eq(event.postId().toString()), eq(KafkaConstants.POST_SYNC_TOPIC));
    }

    @Test
    void handle_Kafka_발행_실패_markSent_미호출() {
        // given
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka 연결 실패"));
        given(objectMapper.writeValueAsString(any())).willReturn("{}");
        given(kafkaTemplate.send(anyString(), anyString(), anyString())).willReturn(future);

        // when
        postSyncKafkaProducer.handle(event);

        // then
        then(outboxEventService).should(never()).markSent(anyString(), anyString());
    }
}
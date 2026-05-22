package com.study.platform.domain.post.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.domain.post.model.FailedPostSync;
import com.study.platform.domain.post.model.FailedPostSyncRepository;
import com.study.platform.global.constant.KafkaConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostSyncDltConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private FailedPostSyncRepository failedPostSyncRepository;

    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private PostSyncDltConsumer postSyncDltConsumer;

    private UUID postId;
    private String payload;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        payload = "{}";
    }

    @Test
    void consume_재시도횟수_미만_메인토픽_재투입() throws Exception {
        // given
        PostSyncEvent event = new PostSyncEvent(postId, PostSyncOperationType.UPSERT, 1);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        given(objectMapper.readValue(payload, PostSyncEvent.class)).willReturn(event);
        given(objectMapper.writeValueAsString(any())).willReturn(payload);
        given(kafkaTemplate.send(eq(KafkaConstants.POST_SYNC_TOPIC), any(String.class))).willReturn(future);

        // when
        postSyncDltConsumer.consume(payload, ack, "ES 연결 실패");

        // then
        then(kafkaTemplate).should().send(eq(KafkaConstants.POST_SYNC_TOPIC), any(String.class));
        then(failedPostSyncRepository).should(never()).save(any());
        then(ack).should().acknowledge();
    }

    @Test
    void consume_재시도횟수_초과_DB_영구저장() throws Exception {
        // given
        PostSyncEvent event = new PostSyncEvent(postId, PostSyncOperationType.UPSERT, KafkaConstants.MAX_DLT_RETRY);
        given(objectMapper.readValue(payload, PostSyncEvent.class)).willReturn(event);

        // when
        postSyncDltConsumer.consume(payload, ack, "ES 연결 실패");

        // then
        then(failedPostSyncRepository).should().save(any(FailedPostSync.class));
        then(kafkaTemplate).should(never()).send(any(), any(String.class));
        then(ack).should().acknowledge();
    }

    @Test
    void consume_페이로드_파싱_실패_ack_처리() throws Exception {
        // given
        given(objectMapper.readValue(payload, PostSyncEvent.class)).willThrow(new RuntimeException("파싱 실패"));

        // when
        postSyncDltConsumer.consume(payload, ack, null);

        // then
        then(kafkaTemplate).should(never()).send(any(), any(String.class));
        then(failedPostSyncRepository).should(never()).save(any());
        then(ack).should().acknowledge();
    }
}
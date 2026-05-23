package com.study.platform.domain.post.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.domain.post.model.FailedPostSync;
import com.study.platform.domain.post.model.FailedPostSyncRepository;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.global.constant.KafkaConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostSyncKafkaConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private StudyPostRepository studyPostRepository;

    @Mock
    private PostSearchService postSearchService;

    @Mock
    private FailedPostSyncRepository failedPostSyncRepository;

    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private PostSyncKafkaConsumer postSyncKafkaConsumer;

    private UUID postId;
    private String payload;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        payload = "{}";
    }

    @Test
    void consume_최대재시도초과_정상흐름_건너뛰고_DB저장() throws Exception {
        // given
        PostSyncEvent event = new PostSyncEvent(postId, PostSyncOperationType.UPSERT, null, KafkaConstants.MAX_DLT_RETRY);
        given(objectMapper.readValue(payload, PostSyncEvent.class)).willReturn(event);

        // when
        postSyncKafkaConsumer.consume(payload, ack);

        // then
        then(failedPostSyncRepository).should().save(any(FailedPostSync.class));
        then(postSearchService).should(never()).index(any());
        then(postSearchService).should(never()).delete(any());
        then(ack).should().acknowledge();
    }

    @Test
    void consume_UPSERT_정상처리() throws Exception {
        // given
        PostSyncEvent event = new PostSyncEvent(postId, PostSyncOperationType.UPSERT, null, 0);
        given(objectMapper.readValue(payload, PostSyncEvent.class)).willReturn(event);
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.empty());

        // when
        postSyncKafkaConsumer.consume(payload, ack);

        // then
        then(failedPostSyncRepository).should(never()).save(any());
        then(ack).should().acknowledge();
    }

    @Test
    void consume_DELETE_정상처리() throws Exception {
        // given
        PostSyncEvent event = new PostSyncEvent(postId, PostSyncOperationType.DELETE, null, 0);
        given(objectMapper.readValue(payload, PostSyncEvent.class)).willReturn(event);

        // when
        postSyncKafkaConsumer.consume(payload, ack);

        // then
        then(postSearchService).should().delete(postId.toString());
        then(failedPostSyncRepository).should(never()).save(any());
        then(ack).should().acknowledge();
    }
}
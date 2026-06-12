package com.study.platform.domain.post.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.support.fake.FakeFailedPostSyncRepository;
import com.study.platform.support.fake.FakeStudyPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostSyncKafkaConsumerTest {

    @Mock
    private PostSearchService postSearchService;

    @Mock
    private Acknowledgment ack;

    private FakeStudyPostRepository studyPostRepository;
    private FakeFailedPostSyncRepository failedPostSyncRepository;
    private PostSyncKafkaConsumer postSyncKafkaConsumer;
    private ObjectMapper objectMapper;

    private UUID postId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        studyPostRepository = new FakeStudyPostRepository();
        failedPostSyncRepository = new FakeFailedPostSyncRepository();
        postSyncKafkaConsumer = new PostSyncKafkaConsumer(objectMapper, studyPostRepository, postSearchService, failedPostSyncRepository);
        postId = UUID.randomUUID();
    }

    @Test
    void consume_최대재시도초과_정상흐름_건너뛰고_DB저장() throws Exception {
        // given
        PostSyncEvent event = new PostSyncEvent(postId, PostSyncOperationType.UPSERT, null, KafkaConstants.MAX_DLT_RETRY);

        // when
        postSyncKafkaConsumer.consume(objectMapper.writeValueAsString(event), ack);

        // then
        assertThat(failedPostSyncRepository.getSaved()).hasSize(1);
        then(postSearchService).should(never()).index(any());
        then(postSearchService).should(never()).delete(any());
        then(ack).should().acknowledge();
    }

    @Test
    void consume_UPSERT_정상처리() throws Exception {
        // given - studyPostRepository is empty, so findByIdWithAuthor returns Optional.empty()
        PostSyncEvent event = new PostSyncEvent(postId, PostSyncOperationType.UPSERT, null, 0);

        // when
        postSyncKafkaConsumer.consume(objectMapper.writeValueAsString(event), ack);

        // then
        assertThat(failedPostSyncRepository.getSaved()).isEmpty();
        then(postSearchService).should(never()).index(any());
        then(ack).should().acknowledge();
    }

    @Test
    void consume_DELETE_정상처리() throws Exception {
        // given
        PostSyncEvent event = new PostSyncEvent(postId, PostSyncOperationType.DELETE, null, 0);

        // when
        postSyncKafkaConsumer.consume(objectMapper.writeValueAsString(event), ack);

        // then
        then(postSearchService).should().delete(postId.toString());
        assertThat(failedPostSyncRepository.getSaved()).isEmpty();
        then(ack).should().acknowledge();
    }
}

package com.study.platform.domain.post.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.domain.post.model.FailedPostSync;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.support.fake.FakeFailedPostSyncRepository;
import com.study.platform.support.fake.FakeKafkaMessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PostSyncDltConsumerTest {

    @Mock
    private Acknowledgment ack;

    private FakeKafkaMessagePublisher kafkaPublisher;
    private FakeFailedPostSyncRepository failedPostSyncRepository;
    private PostSyncDltConsumer postSyncDltConsumer;
    private ObjectMapper objectMapper;

    private UUID postId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        kafkaPublisher = new FakeKafkaMessagePublisher();
        failedPostSyncRepository = new FakeFailedPostSyncRepository();
        postSyncDltConsumer = new PostSyncDltConsumer(objectMapper, kafkaPublisher, failedPostSyncRepository);
        postId = UUID.randomUUID();
    }

    @Test
    void consume_재시도횟수_미만_메인토픽_재투입() throws Exception {
        // given
        PostSyncEvent event = new PostSyncEvent(postId, PostSyncOperationType.UPSERT, null, 1);

        // when
        postSyncDltConsumer.consume(objectMapper.writeValueAsString(event), ack, "ES 연결 실패");

        // then
        assertThat(kafkaPublisher.wasPublishedTo(KafkaConstants.POST_SYNC_TOPIC)).isTrue();
        assertThat(failedPostSyncRepository.getSaved()).isEmpty();
        then(ack).should().acknowledge();
    }

    @Test
    void consume_재시도횟수_초과_DB_영구저장() throws Exception {
        // given
        PostSyncEvent event = new PostSyncEvent(postId, PostSyncOperationType.UPSERT, null, KafkaConstants.MAX_DLT_RETRY);

        // when
        postSyncDltConsumer.consume(objectMapper.writeValueAsString(event), ack, "ES 연결 실패");

        // then
        assertThat(failedPostSyncRepository.getSaved()).hasSize(1);
        assertThat(failedPostSyncRepository.getSaved().get(0)).isInstanceOf(FailedPostSync.class);
        assertThat(kafkaPublisher.getPublishedTopics()).isEmpty();
        then(ack).should().acknowledge();
    }

    @Test
    void consume_페이로드_파싱_실패_ack_처리() {
        // when
        postSyncDltConsumer.consume("invalid-json", ack, null);

        // then
        assertThat(kafkaPublisher.getPublishedTopics()).isEmpty();
        assertThat(failedPostSyncRepository.getSaved()).isEmpty();
        then(ack).should().acknowledge();
    }
}

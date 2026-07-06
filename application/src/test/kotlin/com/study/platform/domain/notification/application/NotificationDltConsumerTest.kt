package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.model.FailedNotification
import com.study.platform.domain.notification.model.NotificationEvent
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.support.fake.FakeAcknowledgment
import com.study.platform.support.fake.FakeFailedNotificationRepository
import com.study.platform.support.fake.FakeKafkaMessagePublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class NotificationDltConsumerTest {

    private lateinit var ack: FakeAcknowledgment

    private lateinit var kafkaPublisher: FakeKafkaMessagePublisher
    private lateinit var failedNotificationRepository: FakeFailedNotificationRepository
    private lateinit var notificationDltConsumer: NotificationDltConsumer
    private lateinit var objectMapper: ObjectMapper

    private lateinit var receiverId: UUID
    private lateinit var targetId: UUID

    @BeforeEach
    fun setUp() {
        ack = FakeAcknowledgment()
        objectMapper = ObjectMapper()
        kafkaPublisher = FakeKafkaMessagePublisher()
        failedNotificationRepository = FakeFailedNotificationRepository()
        notificationDltConsumer = NotificationDltConsumer(objectMapper, kafkaPublisher, failedNotificationRepository)
        receiverId = UUID.randomUUID()
        targetId = UUID.randomUUID()
    }

    @Test
    fun `consume_재시도횟수_미만_메인토픽_재투입`() {
        // given
        val event = NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", targetId, 1)

        // when
        notificationDltConsumer.consume(objectMapper.writeValueAsString(event), ack, "처리 실패")

        // then
        assertThat(kafkaPublisher.wasPublishedTo(KafkaConstants.NOTIFICATION_TOPIC)).isTrue()
        assertThat(failedNotificationRepository.getSaved()).isEmpty()
        assertThat(ack.isAcknowledged()).isTrue()
    }

    @Test
    fun `consume_재시도횟수_초과_DB_영구저장`() {
        // given
        val event = NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", targetId, KafkaConstants.MAX_DLT_RETRY)

        // when
        notificationDltConsumer.consume(objectMapper.writeValueAsString(event), ack, "처리 실패")

        // then
        assertThat(failedNotificationRepository.getSaved()).hasSize(1)
        assertThat(failedNotificationRepository.getSaved()[0]).isInstanceOf(FailedNotification::class.java)
        assertThat(kafkaPublisher.getPublishedTopics()).isEmpty()
        assertThat(ack.isAcknowledged()).isTrue()
    }

    @Test
    fun `consume_페이로드_파싱_실패_ack_처리`() {
        // given
        val invalidPayload = "invalid-json"

        // when
        notificationDltConsumer.consume(invalidPayload, ack, null)

        // then
        assertThat(kafkaPublisher.getPublishedTopics()).isEmpty()
        assertThat(failedNotificationRepository.getSaved()).isEmpty()
        assertThat(ack.isAcknowledged()).isTrue()
    }

    @Test
    fun `consume_재투입시_retryCount_증가`() {
        // given
        val event = NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", targetId, 0)

        // when
        notificationDltConsumer.consume(objectMapper.writeValueAsString(event), ack, null)

        // then
        assertThat(kafkaPublisher.wasPublishedTo(KafkaConstants.NOTIFICATION_TOPIC)).isTrue()
        assertThat(kafkaPublisher.getPublished()[0].payload).contains("retryCount")
    }
}

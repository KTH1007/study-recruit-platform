package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.model.NotificationEvent
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.domain.user.model.User
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.support.fake.FakeFailedNotificationRepository
import com.study.platform.support.fake.FakeNotificationRepository
import com.study.platform.support.fake.FakeRedisMessagePublisher
import com.study.platform.support.fake.FakeUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.util.ReflectionTestUtils
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NotificationKafkaConsumerTest {

    @Mock
    private lateinit var ack: Acknowledgment

    private lateinit var userRepository: FakeUserRepository
    private lateinit var notificationRepository: FakeNotificationRepository
    private lateinit var redisMessagePublisher: FakeRedisMessagePublisher
    private lateinit var failedNotificationRepository: FakeFailedNotificationRepository
    private lateinit var notificationKafkaConsumer: NotificationKafkaConsumer
    private lateinit var objectMapper: ObjectMapper

    private lateinit var receiverId: UUID

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        userRepository = FakeUserRepository()
        notificationRepository = FakeNotificationRepository()
        redisMessagePublisher = FakeRedisMessagePublisher()
        failedNotificationRepository = FakeFailedNotificationRepository()
        notificationKafkaConsumer = NotificationKafkaConsumer(
            objectMapper, userRepository, notificationRepository, redisMessagePublisher, failedNotificationRepository
        )
        receiverId = UUID.randomUUID()
    }

    @Test
    fun `consume_최대재시도초과_정상흐름_건너뛰고_DB저장`() {
        // given
        val event = NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", UUID.randomUUID(), KafkaConstants.MAX_DLT_RETRY)

        // when
        notificationKafkaConsumer.consume(objectMapper.writeValueAsString(event), ack)

        // then
        assertThat(failedNotificationRepository.getSaved()).hasSize(1)
        assertThat(userRepository.findById(receiverId)).isNull()
        then(ack).should().acknowledge()
    }

    @Test
    fun `consume_정상처리`() {
        // given
        val event = NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", UUID.randomUUID(), 0)
        val receiver = User.create("kakao1", "수신자", "receiver@test.com")
        ReflectionTestUtils.setField(receiver, "id", receiverId)
        userRepository.save(receiver)

        // when
        notificationKafkaConsumer.consume(objectMapper.writeValueAsString(event), ack)

        // then
        assertThat(notificationRepository.findAllByReceiverId(receiverId)).isNotEmpty()
        assertThat(failedNotificationRepository.getSaved()).isEmpty()
        assertThat(redisMessagePublisher.wasPublishedTo("notification")).isTrue()
        then(ack).should().acknowledge()
    }
}

package com.study.platform.domain.notification.model

import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatNoException
import org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.util.UUID

class NotificationTest {

    private lateinit var receiver: User
    private lateinit var notification: Notification

    @BeforeEach
    fun setUp() {
        receiver = User.create("kakao-1", "수신자", "receiver@test.com")
        ReflectionTestUtils.setField(receiver, "id", UUID.randomUUID())

        notification = Notification.create(receiver, NotificationType.APPLY_RECEIVED, "지원이 도착했습니다.", UUID.randomUUID())
    }

    @Test
    fun `validateReceiver_본인_예외없음`() {
        // given
        val receiverId = receiver.id!!

        // when & then
        assertThatNoException().isThrownBy { notification.validateReceiver(receiverId) }
    }

    @Test
    fun `validateReceiver_본인아님_예외발생`() {
        // given
        val otherId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { notification.validateReceiver(otherId) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.FORBIDDEN)
            }
    }
}

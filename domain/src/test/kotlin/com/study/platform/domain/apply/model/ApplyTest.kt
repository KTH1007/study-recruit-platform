package com.study.platform.domain.apply.model

import com.study.platform.domain.apply.event.ApplyApprovedEvent
import com.study.platform.domain.apply.event.ApplyReceivedEvent
import com.study.platform.domain.apply.event.ApplyRejectedEvent
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.support.TestFixtures
import com.study.platform.support.fake.FakeDomainEventPublisher
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApplyTest {

    private lateinit var author: User
    private lateinit var applicant: User
    private lateinit var post: StudyPost
    private lateinit var eventPublisher: FakeDomainEventPublisher

    @BeforeEach
    fun setUp() {
        author = TestFixtures.createUser(kakaoId = "kakao-author", nickname = "작성자", email = "author@test.com")
        applicant = TestFixtures.createUser(kakaoId = "kakao-applicant", nickname = "지원자", email = "applicant@test.com")
        post = TestFixtures.createStudyPost(author = author)
        eventPublisher = FakeDomainEventPublisher()
    }

    @Test
    fun `create_PENDING상태로_생성되고_ApplyReceivedEvent_발행`() {
        // when
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)

        // then
        assertThat(apply.status).isEqualTo(ApplyStatus.PENDING)
        assertThat(eventPublisher.getEventsOf(ApplyReceivedEvent::class.java)).hasSize(1)
    }

    @Test
    fun `approve_PENDING상태_APPROVED로_전이되고_ApplyApprovedEvent_발행`() {
        // given
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)

        // when
        apply.approve(eventPublisher)

        // then
        assertThat(apply.status).isEqualTo(ApplyStatus.APPROVED)
        assertThat(eventPublisher.getEventsOf(ApplyApprovedEvent::class.java)).hasSize(1)
    }

    @Test
    fun `approve_이미APPROVED_예외발생`() {
        // given
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)
        apply.approve(eventPublisher)

        // when & then
        assertThatThrownBy { apply.approve(eventPublisher) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.APPLICATION_ALREADY_PROCESSED)
            }
    }

    @Test
    fun `approve_이미REJECTED_예외발생`() {
        // given
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)
        apply.reject(eventPublisher)

        // when & then
        assertThatThrownBy { apply.approve(eventPublisher) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.APPLICATION_ALREADY_PROCESSED)
            }
    }

    @Test
    fun `reject_PENDING상태_REJECTED로_전이되고_ApplyRejectedEvent_발행`() {
        // given
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)

        // when
        apply.reject(eventPublisher)

        // then
        assertThat(apply.status).isEqualTo(ApplyStatus.REJECTED)
        assertThat(eventPublisher.getEventsOf(ApplyRejectedEvent::class.java)).hasSize(1)
    }

    @Test
    fun `reject_이미REJECTED_예외발생`() {
        // given
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)
        apply.reject(eventPublisher)

        // when & then
        assertThatThrownBy { apply.reject(eventPublisher) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.APPLICATION_ALREADY_PROCESSED)
            }
    }

    @Test
    fun `reject_이미APPROVED_예외발생`() {
        // given
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)
        apply.approve(eventPublisher)

        // when & then
        assertThatThrownBy { apply.reject(eventPublisher) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.APPLICATION_ALREADY_PROCESSED)
            }
    }

    @Test
    fun `validatePending_PENDING상태_예외없음`() {
        // given
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)

        // when & then
        apply.validatePending()
    }
}

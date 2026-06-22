package com.study.platform.domain.apply.application

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.event.ApplyApprovedEvent
import com.study.platform.domain.apply.event.ApplyReceivedEvent
import com.study.platform.domain.apply.event.ApplyRejectedEvent
import com.study.platform.domain.apply.model.Apply
import com.study.platform.domain.apply.model.ApplyStatus
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.support.fake.FakeApplyQueryPort
import com.study.platform.support.fake.FakeApplyRepository
import com.study.platform.support.fake.FakeDomainEventPublisher
import com.study.platform.support.fake.FakeStudyPostRepository
import com.study.platform.support.fake.FakeUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.UUID

class ApplyServiceTest {

    private lateinit var applyRepository: FakeApplyRepository
    private lateinit var studyPostRepository: FakeStudyPostRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var eventPublisher: FakeDomainEventPublisher

    private lateinit var findAppliesService: FindAppliesService
    private lateinit var applyStudyPostService: ApplyStudyPostService
    private lateinit var cancelApplyService: CancelApplyService
    private lateinit var approveApplyService: ApproveApplyService
    private lateinit var rejectApplyService: RejectApplyService

    private lateinit var authorId: UUID
    private lateinit var applicantId: UUID
    private lateinit var postId: UUID
    private lateinit var author: User
    private lateinit var applicant: User
    private lateinit var post: StudyPost

    @BeforeEach
    fun setUp() {
        applyRepository = FakeApplyRepository()
        studyPostRepository = FakeStudyPostRepository()
        userRepository = FakeUserRepository()
        eventPublisher = FakeDomainEventPublisher()

        findAppliesService = FindAppliesService(FakeApplyQueryPort(applyRepository), studyPostRepository)
        applyStudyPostService = ApplyStudyPostService(applyRepository, studyPostRepository, userRepository, eventPublisher)
        cancelApplyService = CancelApplyService(applyRepository)
        approveApplyService = ApproveApplyService(applyRepository, studyPostRepository, eventPublisher)
        rejectApplyService = RejectApplyService(applyRepository, eventPublisher)

        authorId = UUID.randomUUID()
        applicantId = UUID.randomUUID()
        postId = UUID.randomUUID()

        author = User.create("kakao1", "작성자", "author@test.com")
        ReflectionTestUtils.setField(author, "id", authorId)

        applicant = User.create("kakao2", "지원자", "applicant@test.com")
        ReflectionTestUtils.setField(applicant, "id", applicantId)

        post = StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7))
        ReflectionTestUtils.setField(post, "id", postId)

        studyPostRepository.save(post)
        userRepository.save(applicant)
    }

    @Test
    fun `apply_성공`() {
        // given
        val request = ApplyCreateRequest("지원합니다")

        // when
        val response: ApplyResponse = applyStudyPostService.execute(applicantId, postId, request)

        // then
        assertThat(response.status).isEqualTo(ApplyStatus.PENDING)
        assertThat(applyRepository.existsByPostIdAndApplicantId(postId, applicantId)).isTrue()
        assertThat(eventPublisher.hasEventOf(ApplyReceivedEvent::class.java)).isTrue()
    }

    @Test
    fun `apply_게시글없음_예외발생`() {
        // given
        val request = ApplyCreateRequest("지원합니다")

        // when & then
        assertThatThrownBy { applyStudyPostService.execute(applicantId, UUID.randomUUID(), request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND)
    }

    @Test
    fun `apply_마감된게시글_예외발생`() {
        // given
        post.close()
        val request = ApplyCreateRequest("지원합니다")

        // when & then
        assertThatThrownBy { applyStudyPostService.execute(applicantId, postId, request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_CLOSED)
    }

    @Test
    fun `apply_본인게시글지원_예외발생`() {
        // given
        val request = ApplyCreateRequest("지원합니다")

        // when & then
        assertThatThrownBy { applyStudyPostService.execute(authorId, postId, request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_APPLY_OWN_POST)
    }

    @Test
    fun `apply_중복지원_예외발생`() {
        // given
        val existing = Apply.create(post, applicant, "첫 번째 지원", eventPublisher)
        ReflectionTestUtils.setField(existing, "id", UUID.randomUUID())
        applyRepository.save(existing)
        val request = ApplyCreateRequest("두 번째 지원")

        // when & then
        assertThatThrownBy { applyStudyPostService.execute(applicantId, postId, request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_APPLIED)
    }

    @Test
    fun `approve_성공`() {
        // given
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)
        val applyId = UUID.randomUUID()
        ReflectionTestUtils.setField(apply, "id", applyId)
        applyRepository.save(apply)

        // when
        val response: ApplyResponse = approveApplyService.execute(authorId, applyId)

        // then
        assertThat(response.status).isEqualTo(ApplyStatus.APPROVED)
        assertThat(eventPublisher.hasEventOf(ApplyApprovedEvent::class.java)).isTrue()
    }

    @Test
    fun `approve_정원충족시_게시글마감`() {
        // given
        for (i in 0 until 2) {
            val u = User.create("kakao$i", "user$i", "u$i@test.com")
            ReflectionTestUtils.setField(u, "id", UUID.randomUUID())
            val approved = Apply.create(post, u, "지원", eventPublisher)
            ReflectionTestUtils.setField(approved, "id", UUID.randomUUID())
            applyRepository.save(approved)
            approved.approve(eventPublisher)
        }
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)
        val applyId = UUID.randomUUID()
        ReflectionTestUtils.setField(apply, "id", applyId)
        applyRepository.save(apply)

        // when
        approveApplyService.execute(authorId, applyId)

        // then
        assertThat(post.isOpen()).isFalse()
    }

    @Test
    fun `reject_성공`() {
        // given
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)
        val applyId = UUID.randomUUID()
        ReflectionTestUtils.setField(apply, "id", applyId)
        applyRepository.save(apply)

        // when
        val response: ApplyResponse = rejectApplyService.execute(authorId, applyId)

        // then
        assertThat(response.status).isEqualTo(ApplyStatus.REJECTED)
        assertThat(eventPublisher.hasEventOf(ApplyRejectedEvent::class.java)).isTrue()
    }

    @Test
    fun `cancel_성공`() {
        // given
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)
        val applyId = UUID.randomUUID()
        ReflectionTestUtils.setField(apply, "id", applyId)
        applyRepository.save(apply)

        // when
        cancelApplyService.execute(applicantId, postId)

        // then
        assertThat(applyRepository.findById(applyId)).isNull()
    }

    @Test
    fun `findApplies_성공`() {
        // given
        userRepository.save(author)
        val apply = Apply.create(post, applicant, "지원합니다", eventPublisher)
        ReflectionTestUtils.setField(apply, "id", UUID.randomUUID())
        applyRepository.save(apply)

        // when
        val responses: List<ApplyResponse> = findAppliesService.execute(authorId, postId)

        // then
        assertThat(responses).hasSize(1)
    }

    @Test
    fun `findApplies_작성자아님_예외발생`() {
        // given
        val otherId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { findAppliesService.execute(otherId, postId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }
}

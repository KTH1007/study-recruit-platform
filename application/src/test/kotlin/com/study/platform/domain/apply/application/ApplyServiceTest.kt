package com.study.platform.domain.apply.application

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.event.ApplyApprovedEvent
import com.study.platform.domain.apply.event.ApplyReceivedEvent
import com.study.platform.domain.apply.event.ApplyRejectedEvent
import com.study.platform.domain.apply.model.ApplyStatus
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.support.TestFixtures
import com.study.platform.support.fake.FakeApplyQueryPort
import com.study.platform.support.fake.FakeApplyRepository
import com.study.platform.support.fake.FakeDomainEventPublisher
import com.study.platform.support.fake.FakeStudyPostRepository
import com.study.platform.support.fake.FakeUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
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

        author = TestFixtures.createUser(id = authorId, kakaoId = "kakao1", nickname = "작성자", email = "author@test.com")
        applicant = TestFixtures.createUser(id = applicantId, kakaoId = "kakao2", nickname = "지원자", email = "applicant@test.com")
        post = TestFixtures.createStudyPost(id = postId, author = author, techStack = "Java", maxMembers = 3)

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
        val existing = TestFixtures.createApply(post = post, applicant = applicant, message = "첫 번째 지원")
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
        val applyId = UUID.randomUUID()
        val apply = TestFixtures.createApply(id = applyId, post = post, applicant = applicant)
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
            val u = TestFixtures.createUser(kakaoId = "kakao$i", nickname = "user$i", email = "u$i@test.com")
            val approved = TestFixtures.createApply(post = post, applicant = u, message = "지원")
            applyRepository.save(approved)
            approved.approve(eventPublisher)
        }
        val applyId = UUID.randomUUID()
        val apply = TestFixtures.createApply(id = applyId, post = post, applicant = applicant)
        applyRepository.save(apply)

        // when
        approveApplyService.execute(authorId, applyId)

        // then
        assertThat(post.isOpen()).isFalse()
    }

    @Test
    fun `reject_성공`() {
        // given
        val applyId = UUID.randomUUID()
        val apply = TestFixtures.createApply(id = applyId, post = post, applicant = applicant)
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
        val applyId = UUID.randomUUID()
        val apply = TestFixtures.createApply(id = applyId, post = post, applicant = applicant)
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
        val apply = TestFixtures.createApply(post = post, applicant = applicant)
        applyRepository.save(apply)

        // when
        val responses = findAppliesService.execute(authorId, postId, PageRequest.of(0, 20))

        // then
        assertThat(responses.content).hasSize(1)
    }

    @Test
    fun `findApplies_작성자아님_예외발생`() {
        // given
        val otherId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { findAppliesService.execute(otherId, postId, PageRequest.of(0, 20)) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }
}

package com.study.platform.domain.apply.model

import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.port.ApplyQueryPort
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.support.AbstractIntegrationTest
import com.study.platform.support.fake.FakeDomainEventPublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Transactional
class ApplyRepositoryTest : AbstractIntegrationTest() {

    @Autowired private lateinit var applyRepository: ApplyRepository
    @Autowired private lateinit var applyQueryPort: ApplyQueryPort
    @Autowired private lateinit var studyPostRepository: StudyPostRepository
    @Autowired private lateinit var userRepository: UserRepository

    private lateinit var author: User
    private lateinit var applicant: User
    private lateinit var post: StudyPost
    private lateinit var apply: Apply

    @BeforeEach
    fun setUp() {
        author = userRepository.save(User.create("kakao-1", "작성자", "author@test.com"))
        applicant = userRepository.save(User.create("kakao-2", "지원자", "applicant@test.com"))
        post = studyPostRepository.save(
            StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 5, LocalDateTime.now().plusDays(7))
        )
        apply = applyRepository.save(Apply.create(post, applicant, "지원합니다", FakeDomainEventPublisher()))
        em.flush()
    }

    @Test
    fun `findAllByPostId_성공`() {
        val result: List<ApplyResponse> = applyQueryPort.findAllByPostId(post.id!!)

        assertThat(result).hasSize(1)
        assertThat(result[0].applicantNickname).isEqualTo("지원자")
        assertThat(result[0].message).isEqualTo("지원합니다")
    }

    @Test
    fun `findAllByPostId_지원없음_빈리스트`() {
        val otherPost = studyPostRepository.save(
            StudyPost.create(author, "다른 스터디", "열심히 합니다", "Kotlin", 3, LocalDateTime.now().plusDays(7))
        )

        val result: List<ApplyResponse> = applyQueryPort.findAllByPostId(otherPost.id!!)

        assertThat(result).isEmpty()
    }

    @Test
    fun `findByIdWithPostAndApplicant_성공`() {
        val result: Apply? = applyRepository.findByIdWithPostAndApplicant(apply.id!!)

        assertThat(result).isNotNull()
        assertThat(result!!.post.title).isEqualTo("스터디 모집")
        assertThat(result.applicant.nickname).isEqualTo("지원자")
    }

    @Test
    fun `findByIdWithPostAndApplicant_존재하지않음_빈Optional`() {
        val result: Apply? = applyRepository.findByIdWithPostAndApplicant(UUID.randomUUID())

        assertThat(result).isNull()
    }

    @Test
    fun `findByPostIdAndApplicantId_성공`() {
        val result: Apply? = applyRepository.findByPostIdAndApplicantId(post.id!!, applicant.id!!)

        assertThat(result).isNotNull()
        assertThat(result!!.status).isEqualTo(ApplyStatus.PENDING)
    }

    @Test
    fun `findByPostIdAndApplicantId_존재하지않음_빈Optional`() {
        val result: Apply? = applyRepository.findByPostIdAndApplicantId(post.id!!, author.id!!)

        assertThat(result).isNull()
    }

    @Test
    fun `existsByPostIdAndApplicantId_존재함_true`() {
        val result = applyRepository.existsByPostIdAndApplicantId(post.id!!, applicant.id!!)

        assertThat(result).isTrue()
    }

    @Test
    fun `existsByPostIdAndApplicantId_존재하지않음_false`() {
        val result = applyRepository.existsByPostIdAndApplicantId(post.id!!, author.id!!)

        assertThat(result).isFalse()
    }

    @Test
    fun `countByPostIdAndStatus_PENDING_카운트`() {
        val count = applyRepository.countByPostIdAndStatus(post.id!!, ApplyStatus.PENDING)

        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `countByPostIdAndStatus_APPROVED_카운트`() {
        apply.approve(FakeDomainEventPublisher())
        applyRepository.save(apply)

        val count = applyRepository.countByPostIdAndStatus(post.id!!, ApplyStatus.APPROVED)

        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `countByPostIdAndStatus_없을때_0`() {
        val count = applyRepository.countByPostIdAndStatus(post.id!!, ApplyStatus.APPROVED)

        assertThat(count).isZero()
    }

    @Test
    fun `findByIdWithPostAndApplicantForUpdate_성공`() {
        val result: Apply? = applyRepository.findByIdWithPostAndApplicantForUpdate(apply.id!!)

        assertThat(result).isNotNull()
        assertThat(result!!.id).isEqualTo(apply.id)
    }
}

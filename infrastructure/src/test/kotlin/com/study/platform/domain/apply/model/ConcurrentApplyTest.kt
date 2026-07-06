package com.study.platform.domain.apply.model

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.domain.apply.infrastructure.ApplyJpaRepository
import com.study.platform.domain.apply.usecase.ApplyStudyPostUseCase
import com.study.platform.domain.apply.usecase.ApproveApplyUseCase
import com.study.platform.domain.post.infrastructure.StudyPostJpaRepository
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.team.infrastructure.StudyTeamJpaRepository
import com.study.platform.domain.team.infrastructure.TeamMemberJpaRepository
import com.study.platform.domain.user.infrastructure.UserJpaRepository
import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.kafka.KafkaMessagePublisher
import com.study.platform.global.support.AbstractIntegrationTest
import com.study.platform.support.fake.FakeDomainEventPublisher
import com.study.platform.support.fake.FakeKafkaMessagePublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ConcurrentApplyTest : AbstractIntegrationTest() {

    @TestConfiguration
    class KafkaConfig {
        @Bean
        @Primary
        fun kafkaMessagePublisher(): KafkaMessagePublisher = FakeKafkaMessagePublisher()
    }

    @Autowired private lateinit var applyStudyPostUseCase: ApplyStudyPostUseCase
    @Autowired private lateinit var approveApplyUseCase: ApproveApplyUseCase
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var studyPostRepository: StudyPostRepository
    @Autowired private lateinit var applyRepository: ApplyRepository
    @Autowired private lateinit var applyJpaRepository: ApplyJpaRepository
    @Autowired private lateinit var studyPostJpaRepository: StudyPostJpaRepository
    @Autowired private lateinit var userJpaRepository: UserJpaRepository
    @Autowired private lateinit var teamMemberJpaRepository: TeamMemberJpaRepository
    @Autowired private lateinit var studyTeamJpaRepository: StudyTeamJpaRepository

    private lateinit var author: User
    private lateinit var applicant: User

    @BeforeEach
    fun setUp() {
        val unique = UUID.randomUUID().toString().take(8)
        author = userRepository.save(User.create("kakao-author-$unique", "작성자-$unique", "author-$unique@test.com"))
        applicant = userRepository.save(User.create("kakao-applicant-$unique", "지원자-$unique", "applicant-$unique@test.com"))
    }

    @AfterEach
    fun tearDown() {
        teamMemberJpaRepository.deleteAll()
        studyTeamJpaRepository.deleteAll()
        applyJpaRepository.deleteAll()
        studyPostJpaRepository.deleteAll()
        userJpaRepository.deleteAll()
    }

    @Test
    fun `동시_중복지원_FOR_UPDATE_직렬화로_하나만_저장`() {
        // given
        val post = studyPostRepository.save(
            com.study.platform.domain.post.model.StudyPost.create(
                author, "스터디 모집", "열심히 합니다", "Java", 5, LocalDateTime.now().plusDays(7)
            )
        )
        val postId = checkNotNull(post.id)
        val applicantId = checkNotNull(applicant.id)
        val threadCount = 3
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(1)
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)

        // when
        val futures = (1..threadCount).map {
            executor.submit {
                latch.await()
                try {
                    applyStudyPostUseCase.execute(applicantId, postId, ApplyCreateRequest("지원합니다"))
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failureCount.incrementAndGet()
                }
            }
        }
        latch.countDown()
        futures.forEach { it.get(10, TimeUnit.SECONDS) }
        executor.shutdown()

        // then
        assertThat(successCount.get()).isEqualTo(1)
        assertThat(failureCount.get()).isEqualTo(threadCount - 1)
        assertThat(applyRepository.existsByPostIdAndApplicantId(postId, applicantId)).isTrue()
    }

    @Test
    fun `동시_승인_FOR_UPDATE_직렬화로_정원초과_방지`() {
        // given
        val unique2 = UUID.randomUUID().toString().take(8)
        val applicant2 = userRepository.save(User.create("kakao-a2-$unique2", "지원자2-$unique2", "a2-$unique2@test.com"))
        val post = studyPostRepository.save(
            com.study.platform.domain.post.model.StudyPost.create(
                author, "스터디 모집", "열심히 합니다", "Kotlin", 1, LocalDateTime.now().plusDays(7)
            )
        )
        val authorId = checkNotNull(author.id)
        val eventPublisher = FakeDomainEventPublisher()
        val apply1 = applyRepository.save(Apply.create(post, applicant, "지원합니다1", eventPublisher))
        val apply2 = applyRepository.save(Apply.create(post, applicant2, "지원합니다2", eventPublisher))
        val executor = Executors.newFixedThreadPool(2)
        val latch = CountDownLatch(1)
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)

        // when
        val futures = listOf(apply1.id, apply2.id).map { applyId ->
            executor.submit {
                latch.await()
                try {
                    approveApplyUseCase.execute(authorId, applyId)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failureCount.incrementAndGet()
                }
            }
        }
        latch.countDown()
        futures.forEach { it.get(10, TimeUnit.SECONDS) }
        executor.shutdown()

        // then
        assertThat(successCount.get()).isEqualTo(1)
        assertThat(failureCount.get()).isEqualTo(1)
        val postId = checkNotNull(post.id)
        assertThat(applyRepository.countByPostIdAndStatus(postId, ApplyStatus.APPROVED)).isEqualTo(1)
    }
}

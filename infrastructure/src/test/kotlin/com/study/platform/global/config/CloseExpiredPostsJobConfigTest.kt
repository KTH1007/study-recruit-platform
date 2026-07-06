package com.study.platform.global.config

import com.study.platform.domain.post.infrastructure.StudyPostJpaRepository
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.post.model.StudyPostStatus
import com.study.platform.domain.user.infrastructure.UserJpaRepository
import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.support.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

@SpringBatchTest
class CloseExpiredPostsJobConfigTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    private lateinit var closeExpiredPostsJob: Job

    @Autowired
    private lateinit var studyPostRepository: StudyPostRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var studyPostJpaRepository: StudyPostJpaRepository

    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    private lateinit var author: User

    @BeforeEach
    fun setUp() {
        jobLauncherTestUtils.job = closeExpiredPostsJob
        val unique = UUID.randomUUID().toString().take(8)
        author = userRepository.save(User.create("kakao-$unique", "작성자-$unique", "$unique@test.com"))
    }

    @AfterEach
    fun tearDown() {
        studyPostJpaRepository.deleteAll()
        userJpaRepository.deleteAll()
    }

    @Test
    fun `마감기한이_지난_OPEN_게시글은_CLOSED로_변경된다`() {
        // given
        val expiredPost = studyPostRepository.save(
            StudyPost.create(author, "마감된 스터디", "설명", "Java", 5, LocalDateTime.now().minusDays(1))
        )
        val activePost = studyPostRepository.save(
            StudyPost.create(author, "진행중 스터디", "설명", "Java", 5, LocalDateTime.now().plusDays(7))
        )
        val jobParameters = JobParametersBuilder()
            .addLocalDateTime("runAt", LocalDateTime.now())
            .addString("uniqueness", UUID.randomUUID().toString())
            .toJobParameters()

        // when
        val execution = jobLauncherTestUtils.launchJob(jobParameters)

        // then
        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(checkNotNull(studyPostRepository.findById(checkNotNull(expiredPost.id))).status).isEqualTo(StudyPostStatus.CLOSED)
        assertThat(checkNotNull(studyPostRepository.findById(checkNotNull(activePost.id))).status).isEqualTo(StudyPostStatus.OPEN)
    }

    @Test
    fun `마감기한이_남은_게시글은_변경되지_않는다`() {
        // given
        val activePost = studyPostRepository.save(
            StudyPost.create(author, "진행중 스터디", "설명", "Java", 5, LocalDateTime.now().plusDays(3))
        )
        val jobParameters = JobParametersBuilder()
            .addLocalDateTime("runAt", LocalDateTime.now())
            .addString("uniqueness", UUID.randomUUID().toString())
            .toJobParameters()

        // when
        val execution = jobLauncherTestUtils.launchJob(jobParameters)

        // then
        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(checkNotNull(studyPostRepository.findById(checkNotNull(activePost.id))).status).isEqualTo(StudyPostStatus.OPEN)
    }
}

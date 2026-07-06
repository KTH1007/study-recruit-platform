package com.study.platform.domain.post.application

import com.study.platform.domain.post.event.PostDeadlineReminderEvent
import com.study.platform.support.TestFixtures
import com.study.platform.support.fake.FakeDomainEventPublisher
import com.study.platform.support.fake.FakeStudyPostRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class PostSchedulerTest {

    @Mock
    private lateinit var jobOperator: JobOperator

    @Mock
    private lateinit var closeExpiredPostsJob: Job

    private lateinit var studyPostRepository: FakeStudyPostRepository
    private lateinit var eventPublisher: FakeDomainEventPublisher
    private lateinit var postScheduler: PostScheduler

    @BeforeEach
    fun setUp() {
        studyPostRepository = FakeStudyPostRepository()
        eventPublisher = FakeDomainEventPublisher()
        postScheduler = PostScheduler(studyPostRepository, eventPublisher, jobOperator, closeExpiredPostsJob)
    }

    @Test
    fun `closeExpiredPosts_정상_Job_실행`() {
        // given
        given(jobOperator.start(any(Job::class.java), any(JobParameters::class.java)))
            .willReturn(org.mockito.Mockito.mock(JobExecution::class.java))

        // when & then
        assertThatCode { postScheduler.closeExpiredPosts() }.doesNotThrowAnyException()
        then(jobOperator).should().start(any(Job::class.java), any(JobParameters::class.java))
    }

    @Test
    fun `closeExpiredPosts_Job_실행_실패해도_예외를_전파하지_않는다`() {
        // given
        given(jobOperator.start(any(Job::class.java), any(JobParameters::class.java)))
            .willThrow(RuntimeException("Job 실행 실패"))

        // when & then
        assertThatCode { postScheduler.closeExpiredPosts() }.doesNotThrowAnyException()
    }

    @Test
    fun `sendDeadlineRemainder_마감임박게시글_이벤트발행`() {
        // given
        val author = TestFixtures.createUser()
        val tomorrow = LocalDateTime.now().plusDays(1)
        val reminderPost = TestFixtures.createStudyPost(author = author, deadline = tomorrow)
        studyPostRepository.save(reminderPost)

        // when
        postScheduler.sendDeadlineRemainder()

        // then
        val events = eventPublisher.getEventsOf(PostDeadlineReminderEvent::class.java)
        assertThat(events).hasSize(1)
        assertThat(events[0].postId).isEqualTo(reminderPost.id)
    }

    @Test
    fun `sendDeadlineRemainder_대상없으면_이벤트미발행`() {
        // when
        postScheduler.sendDeadlineRemainder()

        // then
        assertThat(eventPublisher.getEvents()).isEmpty()
    }
}

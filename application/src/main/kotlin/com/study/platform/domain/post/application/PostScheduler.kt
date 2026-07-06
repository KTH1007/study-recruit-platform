package com.study.platform.domain.post.application

import com.study.platform.domain.post.event.PostDeadlineReminderEvent
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.post.model.StudyPostStatus
import com.study.platform.global.constant.TimeConstants
import com.study.platform.global.event.DomainEventPublisher
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobOperator
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Component
@Transactional(readOnly = true)
class PostScheduler(
    private val studyPostRepository: StudyPostRepository,
    private val eventPublisher: DomainEventPublisher,
    private val jobOperator: JobOperator,
    private val closeExpiredPostsJob: Job
) {
    private val log = LoggerFactory.getLogger(PostScheduler::class.java)!!

    companion object {
        private const val RUN_AT_PARAM = "runAt"
    }

    @Scheduled(cron = "0 0 0 * * *", zone = TimeConstants.ASIA_SEOUL)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun closeExpiredPosts() {
        try {
            val params = JobParametersBuilder()
                .addLocalDateTime(RUN_AT_PARAM, LocalDateTime.now(TimeConstants.SEOUL_ZONE))
                .toJobParameters()
            jobOperator.start(closeExpiredPostsJob, params)
            log.info("[Scheduler] 마감 처리 Job 실행")
        } catch (e: Exception) {
            log.error("[Scheduler] 마감 처리 Job 실행 실패", e)
        }
    }

    @Scheduled(cron = "0 0 9 * * *", zone = TimeConstants.ASIA_SEOUL)
    @SchedulerLock(name = "PostScheduler_sendDeadlineRemainder", lockAtMostFor = "10m")
    fun sendDeadlineRemainder() {
        val tomorrow = LocalDate.now(TimeConstants.SEOUL_ZONE).plusDays(1)
        val start = tomorrow.atStartOfDay()
        val end = tomorrow.atTime(LocalTime.MAX)
        val posts = studyPostRepository.findDeadlineReminderPosts(start, end, StudyPostStatus.OPEN)
        posts.forEach { post ->
            val postId = checkNotNull(post.id)
            val authorId = checkNotNull(post.author?.id) { "StudyPost.author.id must not be null" }
            eventPublisher.publish(PostDeadlineReminderEvent(postId, authorId, post.title))
        }
        log.info("[Scheduler] D-1 리마인더 발행: {}건", posts.size)
    }
}

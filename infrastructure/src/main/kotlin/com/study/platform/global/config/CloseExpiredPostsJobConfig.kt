package com.study.platform.global.config

import com.study.platform.domain.post.application.PostSearchService
import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.event.PostSyncOperationType
import com.study.platform.domain.post.model.FailedPostSync
import com.study.platform.domain.post.model.FailedPostSyncRepository
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.post.model.StudyPostStatus
import jakarta.persistence.EntityManagerFactory
import org.slf4j.LoggerFactory
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.listener.SkipListener
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader
import org.springframework.batch.infrastructure.item.database.builder.JpaCursorItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime

@Configuration
class CloseExpiredPostsJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val entityManagerFactory: EntityManagerFactory,
    private val postSearchService: PostSearchService,
    private val studyPostRepository: StudyPostRepository,
    private val failedPostSyncRepository: FailedPostSyncRepository
) {

    private val log = LoggerFactory.getLogger(CloseExpiredPostsJobConfig::class.java)!!

    companion object {
        private const val CHUNK_SIZE = 1000
        private const val JOB_NAME = "closeExpiredPostsJob"
        private const val STEP_NAME = "closeExpiredPostsStep"
        private const val READER_NAME = "expiredPostReader"
        private const val SKIP_LIMIT = 100L
    }

    @Bean
    fun closeExpiredPostsJob(): Job =
        JobBuilder(JOB_NAME, jobRepository)
            .start(closeExpiredPostsStep())
            .build()

    @Bean
    fun closeExpiredPostsStep(): Step =
        StepBuilder(STEP_NAME, jobRepository)
            .chunk<StudyPost, StudyPost>(CHUNK_SIZE)
            .reader(expiredPostReader(null))
            .processor(closePostProcessor())
            .writer(closePostWriter())
            .faultTolerant()
            .skip(Exception::class.java)
            .skipLimit(SKIP_LIMIT)
            .listener(closePostSkipListener())
            .transactionManager(transactionManager)
            .build()

    // ES 색인 실패 등으로 스킵된 항목은 재처리를 위해 감사 테이블에 남긴다.
    private fun closePostSkipListener(): SkipListener<StudyPost, StudyPost> =
        object : SkipListener<StudyPost, StudyPost> {
            override fun onSkipInWrite(item: StudyPost, t: Throwable) {
                val postId = item.id ?: return
                log.error("마감 처리 배치 스킵 - postId={}", postId, t)
                failedPostSyncRepository.save(
                    FailedPostSync.from(
                        PostSyncEvent(postId, PostSyncOperationType.UPSERT, 0L, 0),
                        "마감 처리 배치 실패: ${t.message}"
                    )
                )
            }
        }

    @Bean
    @StepScope
    fun expiredPostReader(
        @Value("#{jobParameters['runAt']}") runAt: LocalDateTime?
    ): JpaCursorItemReader<StudyPost> =
        JpaCursorItemReaderBuilder<StudyPost>()
            .name(READER_NAME)
            .entityManagerFactory(entityManagerFactory)
            .queryString("SELECT p FROM StudyPost p WHERE p.status = :status AND p.deadline < :now")
            .parameterValues(
                hashMapOf<String, Any>().also {
                    it["status"] = StudyPostStatus.OPEN
                    if (runAt != null) it["now"] = runAt
                }
            )
            .build()

    @Bean
    fun closePostProcessor(): ItemProcessor<StudyPost, StudyPost> = ItemProcessor { post ->
        post.close()
        post
    }

    @Bean
    fun closePostWriter(): ItemWriter<StudyPost> = ItemWriter { chunk ->
        @Suppress("UNCHECKED_CAST")
        val posts = chunk.items as List<StudyPost>
        studyPostRepository.saveAll(posts)
        postSearchService.indexAll(posts)
    }
}

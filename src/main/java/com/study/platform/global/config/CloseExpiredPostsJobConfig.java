package com.study.platform.global.config;

import com.study.platform.domain.post.application.PostSearchService;
import com.study.platform.domain.post.document.PostDocument;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.global.constant.TimeConstants;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CloseExpiredPostsJobConfig {

    private static final int CHUNK_SIZE = 1000;
    private static final String JOB_NAME = "closeExpiredPostsJob";
    private static final String STEP_NAME = "closeExpiredPostsStep";
    private static final String READER_NAME = "expiredPostReader";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final PostSearchService postSearchService;
    private final StudyPostRepository studyPostRepository;

    @Bean
    public Job closeExpiredPostsJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(closeExpiredPostsStep())
                .build();
    }

    @Bean
    public Step closeExpiredPostsStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<StudyPost, StudyPost>chunk(CHUNK_SIZE)
                .reader(expiredPostReader())
                .processor(closePostProcessor())
                .writer(closePostWriter())
                .transactionManager(transactionManager)
                .build();
    }

    public JpaCursorItemReader<StudyPost> expiredPostReader() {
        return new JpaCursorItemReaderBuilder<StudyPost>()
                .name(READER_NAME)
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT p FROM StudyPost p WHERE p.status = :status AND p.deadline < :now")
                .parameterValues(Map.of(
                        "status", StudyPostStatus.OPEN,
                        "now", LocalDateTime.now(TimeConstants.SEOUL_ZONE)
                ))
                .build();
    }

    @Bean
    public ItemProcessor<StudyPost, StudyPost> closePostProcessor() {
        return post -> {
            post.close();
            return post;
        };
    }

    @Bean
    public ItemWriter<StudyPost> closePostWriter() {
        return chunk -> {
            studyPostRepository.saveAll(chunk.getItems());
            postSearchService.indexAll(
                    chunk.getItems().stream()
                            .map(PostDocument::from)
                            .toList()
            );
        };
    }
}
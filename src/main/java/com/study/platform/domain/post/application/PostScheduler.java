package com.study.platform.domain.post.application;

import com.study.platform.domain.post.event.PostDeadlineReminderEvent;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.global.constant.TimeConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostScheduler {

    private static final String RUN_AT_PARAM = "runAt";

    private final StudyPostRepository studyPostRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final JobOperator jobOperator;
    private final Job closeExpiredPostsJob;

    @Scheduled(cron = "0 * * * * *", zone = TimeConstants.ASIA_SEOUL) // 매일 자정
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void closeExpiredPosts() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLocalDateTime(RUN_AT_PARAM, LocalDateTime.now(TimeConstants.SEOUL_ZONE))
                    .toJobParameters();
            jobOperator.start(closeExpiredPostsJob, params);
            log.info("[Scheduler] 마감 처리 Job 실행");
        } catch (Exception e) {
            log.error("[Scheduler] 마감 처리 Job 실행 실패", e);
        }
    }

    @Scheduled(cron = "0 0 9 * * *", zone = TimeConstants.ASIA_SEOUL) // 매일 오전 9시
    public void sendDeadlineRemainder() {
        LocalDate tomorrow = LocalDate.now(TimeConstants.SEOUL_ZONE).plusDays(1);
        LocalDateTime start = tomorrow.atStartOfDay();
        LocalDateTime end = tomorrow.atTime(LocalTime.MAX);
        List<StudyPost> posts = studyPostRepository.findDeadlineReminderPosts(start, end, StudyPostStatus.OPEN);
        posts.forEach(post -> eventPublisher.publishEvent(new PostDeadlineReminderEvent(
                post.getId(),
                post.getAuthor().getId(),
                post.getTitle()
        )));
        log.info("[Scheduler] D-1 리마인더 발행: {}건", posts.size());
    }
}

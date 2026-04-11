package com.study.platform.domain.post.application;

import com.study.platform.domain.post.document.PostDocument;
import com.study.platform.domain.post.event.PostDeadlineReminderEvent;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.post.model.StudyPostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.study.platform.global.constant.TimeConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostScheduler {

    private final StudyPostRepository studyPostRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PostSearchService postSearchService;

    @Scheduled(cron = "0 0 0 * * *", zone = TimeConstants.ASIA_SEOUL) // 매일 자정
    @Transactional
    public void closeExpiredPosts() {
        LocalDateTime now = LocalDateTime.now(TimeConstants.SEOUL_ZONE);
        List<StudyPost> expiredPosts = studyPostRepository.findExpiredPosts(now, StudyPostStatus.OPEN);
        expiredPosts.forEach(post -> {
            post.close();
            postSearchService.index(PostDocument.from(post));
        });
        log.info("[Scheduler] 마감 처리 완료: {}건", expiredPosts.size());
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

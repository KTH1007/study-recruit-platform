package com.study.platform.domain.post.application;

import com.study.platform.domain.post.event.PostDeadlineReminderEvent;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostScheduler {

    private final StudyPostRepository studyPostRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 매일 자정
    @Transactional
    public void closeExpiredPosts() {
        List<StudyPost> expiredPosts = studyPostRepository.findExpiredPosts(LocalDateTime.now());
        expiredPosts.forEach(StudyPost::close);
        log.info("[Scheduler] 마감 처리 완료: {}건", expiredPosts.size());
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul") // 매일 오전 9시
    public void sendDeadlineRemainder() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        List<StudyPost> posts = studyPostRepository.findDeadlineReminderPosts(start, end);
        posts.forEach(post -> eventPublisher.publishEvent(new PostDeadlineReminderEvent(
                post.getId(),
                post.getAuthor().getId(),
                post.getTitle()
        )));
        log.info("[Scheduler] D-1 리마인더 발행: {}건", posts.size());
    }
}

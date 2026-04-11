package com.study.platform.domain.team.application;

import com.study.platform.domain.notification.application.NotificationKafkaProducer;
import com.study.platform.domain.notification.application.NotificationService;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.team.model.TeamMemberRepository;
import com.study.platform.domain.team.model.TeamSchedule;
import com.study.platform.domain.team.model.TeamScheduleRepository;
import com.study.platform.global.constant.TimeConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamScheduleReminderScheduler {

    private final TeamScheduleRepository teamScheduleRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final NotificationKafkaProducer kafkaProducer;

    @Scheduled(cron = "0 0 9 * * *", zone = TimeConstants.ASIA_SEOUL)
    public void sendScheduleReminder() {
        LocalDate tomorrow = LocalDate.now(TimeConstants.SEOUL_ZONE).plusDays(1);
        LocalDateTime start = tomorrow.atStartOfDay();
        LocalDateTime end = tomorrow.atTime(LocalTime.MAX);

        List<TeamSchedule> schedules = teamScheduleRepository.findAllByScheduledAtBetweenWithTeam(start, end);
        log.info("[Scheduler] D-1 일정 리마인더 발송 시작 - 대상 일정 수: {}", schedules.size());

        schedules.forEach(schedule -> {
            String message = schedule.getTeam().getName() + " " + NotificationType.SCHEDULE_REMINDER.getDescription();
            teamMemberRepository.findAllByTeamId(schedule.getTeam().getId()).forEach(member ->
                    kafkaProducer.send(
                            member.getUser().getId(),
                            NotificationType.SCHEDULE_REMINDER,
                            message,
                            schedule.getId()
                    )
            );
        });

        log.info("[Scheduler] D-1 일정 리마인더 발송 완료");
    }
}

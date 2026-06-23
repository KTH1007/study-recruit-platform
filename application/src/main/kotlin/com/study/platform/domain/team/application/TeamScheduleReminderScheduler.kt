package com.study.platform.domain.team.application

import com.study.platform.domain.notification.application.NotificationKafkaProducer
import com.study.platform.domain.notification.model.NotificationEvent
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.team.model.TeamScheduleRepository
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.constant.TimeConstants
import com.study.platform.global.outbox.application.OutboxEventService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.time.LocalTime

@Component
class TeamScheduleReminderScheduler(
    private val teamScheduleRepository: TeamScheduleRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val kafkaProducer: NotificationKafkaProducer,
    private val outboxEventService: OutboxEventService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(TeamScheduleReminderScheduler::class.java)!!

    @Scheduled(cron = "0 0 9 * * *", zone = TimeConstants.ASIA_SEOUL)
    fun sendScheduleReminder() {
        val tomorrow = LocalDate.now(TimeConstants.SEOUL_ZONE).plusDays(1)
        val start = tomorrow.atStartOfDay()
        val end = tomorrow.atTime(LocalTime.MAX)

        val schedules = teamScheduleRepository.findAllByScheduledAtBetweenWithTeam(start, end)
        log.info("[Scheduler] D-1 일정 리마인더 발송 시작 - 대상 일정 수: {}", schedules.size)

        schedules.forEach { schedule ->
            val teamName = schedule.team!!.name
            val teamId = schedule.team!!.id!!
            val scheduleId = schedule.id!!
            val message = "$teamName ${NotificationType.SCHEDULE_REMINDER.description}"
            teamMemberRepository.findAllByTeamId(teamId).forEach { member ->
                val userId = member.user!!.id!!
                val payload = objectMapper.writeValueAsString(
                    NotificationEvent(userId, NotificationType.SCHEDULE_REMINDER, message, scheduleId, 0)
                )
                val outboxEventId = outboxEventService.saveWithNewTx(KafkaConstants.NOTIFICATION_TOPIC, userId.toString(), payload)
                kafkaProducer.send(outboxEventId, userId, NotificationType.SCHEDULE_REMINDER, message, scheduleId)
            }
        }

        log.info("[Scheduler] D-1 일정 리마인더 발송 완료")
    }
}

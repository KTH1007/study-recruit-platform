package com.study.platform.domain.team.application

import com.study.platform.domain.notification.application.NotificationKafkaProducer
import com.study.platform.domain.notification.event.NotificationEvent
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.team.model.TeamSchedule
import com.study.platform.domain.team.model.TeamScheduleRepository
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.constant.TimeConstants
import com.study.platform.global.outbox.application.OutboxEventService
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
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

    companion object {
        private const val SCHEDULE_BATCH_SIZE = 100
    }

    @Scheduled(cron = "0 0 9 * * *", zone = TimeConstants.ASIA_SEOUL)
    @SchedulerLock(name = "TeamScheduleReminderScheduler_sendScheduleReminder", lockAtMostFor = "10m")
    fun sendScheduleReminder() {
        val tomorrow = LocalDate.now(TimeConstants.SEOUL_ZONE).plusDays(1)
        val start = tomorrow.atStartOfDay()
        val end = tomorrow.atTime(LocalTime.MAX)

        log.info("[Scheduler] D-1 일정 리마인더 발송 시작")

        var totalScheduleCount = 0
        var pageable: Pageable = PageRequest.of(0, SCHEDULE_BATCH_SIZE)
        while (true) {
            val slice = teamScheduleRepository.findAllByScheduledAtBetweenWithTeam(start, end, pageable)
            sendReminderForSchedules(slice.content)
            totalScheduleCount += slice.content.size

            if (!slice.hasNext()) break
            pageable = pageable.next()
        }

        log.info("[Scheduler] D-1 일정 리마인더 발송 완료 - 대상 일정 수: {}", totalScheduleCount)
    }

    private fun sendReminderForSchedules(schedules: List<TeamSchedule>) {
        if (schedules.isEmpty()) return

        val teamIds = schedules.mapNotNull { it.team?.id }.distinct()
        val membersByTeamId = teamMemberRepository.findAllByTeamIdIn(teamIds).groupBy { it.team?.id }

        schedules.forEach { schedule ->
            val team = checkNotNull(schedule.team) { "TeamSchedule.team must not be null" }
            val teamId = checkNotNull(team.id)
            val scheduleId = checkNotNull(schedule.id)
            val message = "${team.name} ${NotificationType.SCHEDULE_REMINDER.description}"
            membersByTeamId[teamId].orEmpty().forEach { member ->
                val userId = checkNotNull(member.user?.id) { "TeamMember.user.id must not be null" }
                val outboxEventId = outboxEventService.saveWithIdEmbedded(KafkaConstants.NOTIFICATION_TOPIC, userId.toString()) { id ->
                    objectMapper.writeValueAsString(NotificationEvent(userId, NotificationType.SCHEDULE_REMINDER, message, scheduleId, 0, id))
                }
                kafkaProducer.send(outboxEventId, userId, NotificationType.SCHEDULE_REMINDER, message, scheduleId)
            }
        }
    }
}

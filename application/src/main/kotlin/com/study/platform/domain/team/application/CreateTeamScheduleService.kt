package com.study.platform.domain.team.application

import com.study.platform.domain.notification.event.NotificationEvent
import com.study.platform.domain.notification.model.NotificationPublisher
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest
import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import com.study.platform.domain.team.model.StudyTeamRepository
import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.team.model.TeamSchedule
import com.study.platform.domain.team.model.TeamScheduleRepository
import com.study.platform.domain.team.usecase.CreateTeamScheduleUseCase
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.outbox.application.OutboxEventService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Service
class CreateTeamScheduleService(
    private val teamScheduleRepository: TeamScheduleRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val studyTeamRepository: StudyTeamRepository,
    private val notificationPublisher: NotificationPublisher,
    private val outboxEventService: OutboxEventService,
    private val objectMapper: ObjectMapper
) : CreateTeamScheduleUseCase {

    @Transactional
    override fun execute(userId: UUID, teamId: UUID, request: TeamScheduleCreateRequest): TeamScheduleResponse {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw CustomException(ErrorCode.NOT_TEAM_MEMBER)
        }
        val team = studyTeamRepository.findById(teamId)
            ?: throw CustomException(ErrorCode.TEAM_NOT_FOUND)
        val schedule = teamScheduleRepository.save(
            TeamSchedule.create(team, request.title, request.description, request.scheduledAt!!)
        )
        notifyAllMembers(teamId, team.name, schedule.id!!)
        return TeamScheduleResponse.from(schedule)
    }

    private fun notifyAllMembers(teamId: UUID, teamName: String, scheduledId: UUID) {
        val message = "$teamName ${NotificationType.SCHEDULE_CREATED.description}"
        data class PendingNotification(val outboxEventId: Long, val userId: UUID)

        val pending = teamMemberRepository.findAllByTeamId(teamId).map { member ->
            val userId = member.user!!.id!!
            val outboxEventId = outboxEventService.saveWithIdEmbeddedInTx(KafkaConstants.NOTIFICATION_TOPIC, userId.toString()) { id ->
                objectMapper.writeValueAsString(NotificationEvent(userId, NotificationType.SCHEDULE_CREATED, message, scheduledId, 0, id))
            }
            PendingNotification(outboxEventId, userId)
        }

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    pending.forEach { (outboxEventId, userId) ->
                        notificationPublisher.send(outboxEventId, userId, NotificationType.SCHEDULE_CREATED, message, scheduledId)
                    }
                }
            })
        } else {
            pending.forEach { (outboxEventId, userId) ->
                notificationPublisher.send(outboxEventId, userId, NotificationType.SCHEDULE_CREATED, message, scheduledId)
            }
        }
    }
}

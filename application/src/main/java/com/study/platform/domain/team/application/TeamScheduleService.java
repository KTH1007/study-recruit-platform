package com.study.platform.domain.team.application;

import com.study.platform.domain.notification.model.NotificationEvent;
import com.study.platform.domain.notification.model.NotificationPublisher;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest;
import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest;
import com.study.platform.domain.team.dto.response.TeamScheduleResponse;
import com.study.platform.domain.team.model.*;
import com.study.platform.domain.team.port.TeamScheduleQueryPort;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.outbox.application.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamScheduleService {

    private final TeamScheduleRepository teamScheduleRepository;
    private final TeamScheduleQueryPort teamScheduleQueryPort;
    private final TeamMemberRepository teamMemberRepository;
    private final StudyTeamRepository studyTeamRepository;
    private final NotificationPublisher notificationPublisher;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;

    @Transactional
    public TeamScheduleResponse createSchedule(UUID userId, UUID teamId, TeamScheduleCreateRequest request) {
        validateTeamMember(teamId, userId);
        StudyTeam team = getTeam(teamId);
        TeamSchedule schedule = teamScheduleRepository.save(
                TeamSchedule.create(team, request.title(), request.description(), request.scheduledAt())
        );
        notifyAllMembers(teamId, team.getName(), schedule.getId());
        return TeamScheduleResponse.from(schedule);
    }

    public List<TeamScheduleResponse> findSchedules(UUID userId, UUID teamId) {
        validateTeamMember(teamId, userId);
        return teamScheduleQueryPort.findAllByTeamId(teamId);
    }

    @Transactional
    public TeamScheduleResponse updateSchedule(UUID userId, UUID teamId, UUID scheduleId, TeamScheduleUpdateRequest request) {
        validateTeamMember(teamId, userId);
        TeamSchedule schedule = getSchedule(scheduleId);
        schedule.update(request.title(), request.description(), request.scheduledAt());
        return TeamScheduleResponse.from(schedule);
    }

    @Transactional
    public void deleteSchedule(UUID userId, UUID teamId, UUID scheduledId) {
        validateTeamMember(teamId, userId);
        TeamSchedule schedule = getSchedule(scheduledId);
        teamScheduleRepository.delete(schedule);
    }

    private void notifyAllMembers(UUID teamId, String teamName, UUID scheduledId) {
        String message = teamName + " " + NotificationType.SCHEDULE_CREATED.getDescription();
        teamMemberRepository.findAllByTeamId(teamId).forEach(member -> {
            String payload = objectMapper.writeValueAsString(
                    new NotificationEvent(member.getUser().getId(), NotificationType.SCHEDULE_CREATED, message, scheduledId, 0));
            Long outboxEventId = outboxEventService.save(KafkaConstants.NOTIFICATION_TOPIC, member.getUser().getId().toString(), payload);
            notificationPublisher.send(outboxEventId, member.getUser().getId(), NotificationType.SCHEDULE_CREATED, message, scheduledId);
        });
    }

    private void validateTeamMember(UUID teamId, UUID userId) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_MEMBER);
        }
    }

    private StudyTeam getTeam(UUID teamId) {
        return studyTeamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
    }

    private TeamSchedule getSchedule(UUID scheduleId) {
        return teamScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_SCHEDULE_NOT_FOUND));
    }
}

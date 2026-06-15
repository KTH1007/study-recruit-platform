package com.study.platform.domain.team.application;

import com.study.platform.domain.notification.model.NotificationEvent;
import com.study.platform.domain.notification.model.NotificationPublisher;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest;
import com.study.platform.domain.team.dto.response.TeamScheduleResponse;
import com.study.platform.domain.team.model.*;
import com.study.platform.domain.team.usecase.CreateTeamScheduleUseCase;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.outbox.application.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateTeamScheduleService implements CreateTeamScheduleUseCase {

    private final TeamScheduleRepository teamScheduleRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final StudyTeamRepository studyTeamRepository;
    private final NotificationPublisher notificationPublisher;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TeamScheduleResponse execute(UUID userId, UUID teamId, TeamScheduleCreateRequest request) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_MEMBER);
        }
        StudyTeam team = studyTeamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        TeamSchedule schedule = teamScheduleRepository.save(
                TeamSchedule.create(team, request.title(), request.description(), request.scheduledAt())
        );
        notifyAllMembers(teamId, team.getName(), schedule.getId());
        return TeamScheduleResponse.from(schedule);
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
}

package com.study.platform.domain.team.application;

import com.study.platform.domain.notification.application.NotificationKafkaProducer;
import com.study.platform.domain.notification.application.NotificationService;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest;
import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest;
import com.study.platform.domain.team.dto.response.TeamScheduleResponse;
import com.study.platform.domain.team.model.*;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamScheduleService {

    private final TeamScheduleRepository teamScheduleRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final StudyTeamRepository studyTeamRepository;
    private final NotificationKafkaProducer kafkaProducer;

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
        return teamScheduleRepository.findAllByTeamIdOrderByScheduledAtAsc(teamId).stream()
                .map(TeamScheduleResponse::from)
                .toList();
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
        teamMemberRepository.findAllByTeamId(teamId).forEach(member ->
                kafkaProducer.send(
                        member.getUser().getId(),
                        NotificationType.SCHEDULE_CREATED,
                        message,
                        scheduledId
                ));
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

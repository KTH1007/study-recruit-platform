package com.study.platform.domain.team.application;

import com.study.platform.domain.notification.application.NotificationKafkaProducer;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest;
import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest;
import com.study.platform.domain.team.dto.response.TeamScheduleResponse;
import com.study.platform.domain.team.model.*;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class TeamScheduleServiceTest {

    @Mock private TeamScheduleRepository teamScheduleRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private StudyTeamRepository studyTeamRepository;
    @Mock private NotificationKafkaProducer kafkaProducer;

    @InjectMocks
    private TeamScheduleService teamScheduleService;

    private UUID userId;
    private UUID teamId;
    private UUID scheduleId;
    private User user;
    private StudyPost post;
    private StudyTeam team;
    private TeamMember teamMember;
    private TeamSchedule schedule;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();

        user = User.create("kakao-1", "팀원", "member@test.com");
        ReflectionTestUtils.setField(user, "id", userId);

        post = com.study.platform.domain.post.model.StudyPost.create(
                user, "스터디", "열심히", "Java", 5, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(post, "id", UUID.randomUUID());

        team = StudyTeam.create(post);
        ReflectionTestUtils.setField(team, "id", teamId);

        teamMember = TeamMember.createMember(team, user);

        schedule = TeamSchedule.create(team, "1회차 미팅", "미팅 내용", LocalDateTime.now().plusDays(3));
        ReflectionTestUtils.setField(schedule, "id", scheduleId);
    }

    @Test
    void createSchedule_성공() {
        // given
        TeamScheduleCreateRequest request = new TeamScheduleCreateRequest(
                "1회차 미팅", "미팅 내용", LocalDateTime.now().plusDays(3));
        given(teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)).willReturn(true);
        given(studyTeamRepository.findById(teamId)).willReturn(Optional.of(team));
        given(teamScheduleRepository.save(any())).willReturn(schedule);
        given(teamMemberRepository.findAllByTeamId(teamId)).willReturn(List.of(teamMember));
        willDoNothing().given(kafkaProducer).send(any(), any(), any(), any());

        // when
        TeamScheduleResponse response = teamScheduleService.createSchedule(userId, teamId, request);

        // then
        assertThat(response.title()).isEqualTo("1회차 미팅");
        then(teamScheduleRepository).should().save(any(TeamSchedule.class));
    }

    @Test
    void createSchedule_팀멤버아님_예외발생() {
        // given
        TeamScheduleCreateRequest request = new TeamScheduleCreateRequest(
                "1회차 미팅", "미팅 내용", LocalDateTime.now().plusDays(3));
        given(teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> teamScheduleService.createSchedule(userId, teamId, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NOT_TEAM_MEMBER.getMessage());
    }

    @Test
    void findSchedules_성공() {
        // given
        given(teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)).willReturn(true);
        given(teamScheduleRepository.findAllByTeamIdOrderByScheduledAtAsc(teamId))
                .willReturn(List.of(schedule));

        // when
        List<TeamScheduleResponse> responses = teamScheduleService.findSchedules(userId, teamId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("1회차 미팅");
    }

    @Test
    void findSchedules_팀멤버아님_예외발생() {
        // given
        given(teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> teamScheduleService.findSchedules(userId, teamId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NOT_TEAM_MEMBER.getMessage());
    }

    @Test
    void updateSchedule_성공() {
        // given
        TeamScheduleUpdateRequest request = new TeamScheduleUpdateRequest(
                "2회차 미팅", "수정된 내용", LocalDateTime.now().plusDays(7));
        given(teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)).willReturn(true);
        given(teamScheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

        // when
        TeamScheduleResponse response = teamScheduleService.updateSchedule(userId, teamId, scheduleId, request);

        // then
        assertThat(response.title()).isEqualTo("2회차 미팅");
        assertThat(response.description()).isEqualTo("수정된 내용");
    }

    @Test
    void updateSchedule_일정없음_예외발생() {
        // given
        TeamScheduleUpdateRequest request = new TeamScheduleUpdateRequest(
                "2회차 미팅", "수정된 내용", LocalDateTime.now().plusDays(7));
        given(teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)).willReturn(true);
        given(teamScheduleRepository.findById(scheduleId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamScheduleService.updateSchedule(userId, teamId, scheduleId, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TEAM_SCHEDULE_NOT_FOUND.getMessage());
    }

    @Test
    void deleteSchedule_성공() {
        // given
        given(teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)).willReturn(true);
        given(teamScheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

        // when
        teamScheduleService.deleteSchedule(userId, teamId, scheduleId);

        // then
        then(teamScheduleRepository).should().delete(schedule);
    }

    @Test
    void deleteSchedule_팀멤버아님_예외발생() {
        // given
        given(teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> teamScheduleService.deleteSchedule(userId, teamId, scheduleId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NOT_TEAM_MEMBER.getMessage());
    }
}

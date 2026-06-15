package com.study.platform.domain.team.application;

import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest;
import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest;
import com.study.platform.domain.team.dto.response.TeamScheduleResponse;
import com.study.platform.domain.team.model.*;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.outbox.application.OutboxEventService;
import com.study.platform.support.fake.FakeNotificationPublisher;
import com.study.platform.support.fake.FakeOutboxEventRepository;
import com.study.platform.support.fake.FakeStudyTeamRepository;
import com.study.platform.support.fake.FakeTeamMemberRepository;
import com.study.platform.support.fake.FakeTeamScheduleQueryPort;
import com.study.platform.support.fake.FakeTeamScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamScheduleServiceTest {

    private FakeTeamScheduleRepository teamScheduleRepository;
    private FakeTeamMemberRepository teamMemberRepository;
    private FakeStudyTeamRepository studyTeamRepository;
    private FakeNotificationPublisher notificationPublisher;
    private OutboxEventService outboxEventService;

    private CreateTeamScheduleService createTeamScheduleService;
    private FindTeamSchedulesService findTeamSchedulesService;
    private UpdateTeamScheduleService updateTeamScheduleService;
    private DeleteTeamScheduleService deleteTeamScheduleService;

    private UUID userId;
    private UUID teamId;
    private UUID scheduleId;
    private User user;
    private StudyTeam team;
    private TeamMember teamMember;
    private TeamSchedule schedule;

    @BeforeEach
    void setUp() {
        teamScheduleRepository = new FakeTeamScheduleRepository();
        teamMemberRepository = new FakeTeamMemberRepository();
        studyTeamRepository = new FakeStudyTeamRepository();
        notificationPublisher = new FakeNotificationPublisher();
        outboxEventService = new OutboxEventService(new FakeOutboxEventRepository());
        ObjectMapper objectMapper = new ObjectMapper();

        createTeamScheduleService = new CreateTeamScheduleService(teamScheduleRepository, teamMemberRepository, studyTeamRepository, notificationPublisher, outboxEventService, objectMapper);
        findTeamSchedulesService = new FindTeamSchedulesService(new FakeTeamScheduleQueryPort(teamScheduleRepository), teamMemberRepository);
        updateTeamScheduleService = new UpdateTeamScheduleService(teamScheduleRepository, teamMemberRepository);
        deleteTeamScheduleService = new DeleteTeamScheduleService(teamScheduleRepository, teamMemberRepository);

        userId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();

        user = User.create("kakao-1", "팀원", "member@test.com");
        ReflectionTestUtils.setField(user, "id", userId);

        StudyPost post = StudyPost.create(
                user, "스터디", "열심히", "Java", 5, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(post, "id", UUID.randomUUID());

        team = StudyTeam.create(post);
        ReflectionTestUtils.setField(team, "id", teamId);

        teamMember = TeamMember.createMember(team, user);
        ReflectionTestUtils.setField(teamMember, "id", UUID.randomUUID());

        schedule = TeamSchedule.create(team, "1회차 미팅", "미팅 내용", LocalDateTime.now().plusDays(3));
        ReflectionTestUtils.setField(schedule, "id", scheduleId);

        studyTeamRepository.save(team);
        teamMemberRepository.save(teamMember);
    }

    @Test
    void createSchedule_성공() {
        // given
        TeamScheduleCreateRequest request = new TeamScheduleCreateRequest(
                "1회차 미팅", "미팅 내용", LocalDateTime.now().plusDays(3));

        // when
        TeamScheduleResponse response = createTeamScheduleService.execute(userId, teamId, request);

        // then
        assertThat(response.title()).isEqualTo("1회차 미팅");
        assertThat(notificationPublisher.hasSentTo(userId)).isTrue();
    }

    @Test
    void createSchedule_팀멤버아님_예외발생() {
        // given
        TeamScheduleCreateRequest request = new TeamScheduleCreateRequest(
                "1회차 미팅", "미팅 내용", LocalDateTime.now().plusDays(3));

        // when & then
        assertThatThrownBy(() -> createTeamScheduleService.execute(UUID.randomUUID(), teamId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_TEAM_MEMBER);
    }

    @Test
    void findSchedules_성공() {
        // given
        teamScheduleRepository.save(schedule);

        // when
        List<TeamScheduleResponse> responses = findTeamSchedulesService.execute(userId, teamId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("1회차 미팅");
    }

    @Test
    void findSchedules_팀멤버아님_예외발생() {
        // when & then
        assertThatThrownBy(() -> findTeamSchedulesService.execute(UUID.randomUUID(), teamId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_TEAM_MEMBER);
    }

    @Test
    void updateSchedule_성공() {
        // given
        teamScheduleRepository.save(schedule);
        TeamScheduleUpdateRequest request = new TeamScheduleUpdateRequest(
                "2회차 미팅", "수정된 내용", LocalDateTime.now().plusDays(7));

        // when
        TeamScheduleResponse response = updateTeamScheduleService.execute(userId, teamId, scheduleId, request);

        // then
        assertThat(response.title()).isEqualTo("2회차 미팅");
        assertThat(response.description()).isEqualTo("수정된 내용");
    }

    @Test
    void updateSchedule_일정없음_예외발생() {
        // given
        TeamScheduleUpdateRequest request = new TeamScheduleUpdateRequest(
                "2회차 미팅", "수정된 내용", LocalDateTime.now().plusDays(7));

        // when & then
        assertThatThrownBy(() -> updateTeamScheduleService.execute(userId, teamId, UUID.randomUUID(), request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_SCHEDULE_NOT_FOUND);
    }

    @Test
    void deleteSchedule_성공() {
        // given
        teamScheduleRepository.save(schedule);

        // when
        deleteTeamScheduleService.execute(userId, teamId, scheduleId);

        // then
        assertThat(teamScheduleRepository.findById(scheduleId)).isEmpty();
    }

    @Test
    void deleteSchedule_팀멤버아님_예외발생() {
        // when & then
        assertThatThrownBy(() -> deleteTeamScheduleService.execute(UUID.randomUUID(), teamId, scheduleId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_TEAM_MEMBER);
    }
}

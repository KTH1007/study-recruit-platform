package com.study.platform.domain.team.application

import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest
import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest
import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import com.study.platform.domain.team.model.StudyTeam
import com.study.platform.domain.team.model.TeamMember
import com.study.platform.domain.team.model.TeamSchedule
import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.outbox.application.OutboxEventService
import com.study.platform.support.TestFixtures
import com.study.platform.support.fake.FakeNotificationPublisher
import com.study.platform.support.fake.FakeOutboxEventRepository
import com.study.platform.support.fake.FakeStudyTeamRepository
import com.study.platform.support.fake.FakeTeamMemberRepository
import com.study.platform.support.fake.FakeTeamScheduleQueryPort
import com.study.platform.support.fake.FakeTeamScheduleRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.util.UUID

class TeamScheduleServiceTest {

    private lateinit var teamScheduleRepository: FakeTeamScheduleRepository
    private lateinit var teamMemberRepository: FakeTeamMemberRepository
    private lateinit var studyTeamRepository: FakeStudyTeamRepository
    private lateinit var notificationPublisher: FakeNotificationPublisher
    private lateinit var outboxEventService: OutboxEventService

    private lateinit var createTeamScheduleService: CreateTeamScheduleService
    private lateinit var findTeamSchedulesService: FindTeamSchedulesService
    private lateinit var updateTeamScheduleService: UpdateTeamScheduleService
    private lateinit var deleteTeamScheduleService: DeleteTeamScheduleService

    private lateinit var userId: UUID
    private lateinit var teamId: UUID
    private lateinit var scheduleId: UUID
    private lateinit var user: User
    private lateinit var team: StudyTeam
    private lateinit var teamMember: TeamMember
    private lateinit var schedule: TeamSchedule

    @BeforeEach
    fun setUp() {
        teamScheduleRepository = FakeTeamScheduleRepository()
        teamMemberRepository = FakeTeamMemberRepository()
        studyTeamRepository = FakeStudyTeamRepository()
        notificationPublisher = FakeNotificationPublisher()
        outboxEventService = OutboxEventService(FakeOutboxEventRepository())
        val objectMapper = ObjectMapper()

        createTeamScheduleService = CreateTeamScheduleService(teamScheduleRepository, teamMemberRepository, studyTeamRepository, notificationPublisher, outboxEventService, objectMapper)
        findTeamSchedulesService = FindTeamSchedulesService(FakeTeamScheduleQueryPort(teamScheduleRepository), teamMemberRepository)
        updateTeamScheduleService = UpdateTeamScheduleService(teamScheduleRepository, teamMemberRepository)
        deleteTeamScheduleService = DeleteTeamScheduleService(teamScheduleRepository, teamMemberRepository)

        userId = UUID.randomUUID()
        teamId = UUID.randomUUID()
        scheduleId = UUID.randomUUID()

        user = TestFixtures.createUser(id = userId, kakaoId = "kakao-1", nickname = "팀원", email = "member@test.com")
        val post = TestFixtures.createStudyPost(author = user, techStack = "Java")
        team = TestFixtures.createStudyTeam(id = teamId, post = post)
        teamMember = TestFixtures.createNormalMember(team = team, user = user)
        schedule = TestFixtures.createTeamSchedule(id = scheduleId, team = team, title = "1회차 미팅", description = "미팅 내용")

        studyTeamRepository.save(team)
        teamMemberRepository.save(teamMember)
    }

    @Test
    fun `createSchedule_성공`() {
        // given
        val request = TeamScheduleCreateRequest("1회차 미팅", "미팅 내용", LocalDateTime.now().plusDays(3))

        // when
        val response: TeamScheduleResponse = createTeamScheduleService.execute(userId, teamId, request)

        // then
        assertThat(response.title).isEqualTo("1회차 미팅")
        assertThat(response.title).isEqualTo("1회차 미팅")
    }

    @Test
    fun `createSchedule_팀멤버아님_예외발생`() {
        // given
        val request = TeamScheduleCreateRequest("1회차 미팅", "미팅 내용", LocalDateTime.now().plusDays(3))

        // when & then
        assertThatThrownBy { createTeamScheduleService.execute(UUID.randomUUID(), teamId, request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_TEAM_MEMBER)
    }

    @Test
    fun `findSchedules_성공`() {
        // given
        teamScheduleRepository.save(schedule)

        // when
        val responses: List<TeamScheduleResponse> = findTeamSchedulesService.execute(userId, teamId)

        // then
        assertThat(responses).hasSize(1)
        assertThat(responses[0].title).isEqualTo("1회차 미팅")
    }

    @Test
    fun `findSchedules_팀멤버아님_예외발생`() {
        // given
        val otherId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { findTeamSchedulesService.execute(otherId, teamId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_TEAM_MEMBER)
    }

    @Test
    fun `updateSchedule_성공`() {
        // given
        teamScheduleRepository.save(schedule)
        val request = TeamScheduleUpdateRequest("2회차 미팅", "수정된 내용", LocalDateTime.now().plusDays(7))

        // when
        val response: TeamScheduleResponse = updateTeamScheduleService.execute(userId, teamId, scheduleId, request)

        // then
        assertThat(response.title).isEqualTo("2회차 미팅")
        assertThat(response.description).isEqualTo("수정된 내용")
    }

    @Test
    fun `updateSchedule_일정없음_예외발생`() {
        // given
        val request = TeamScheduleUpdateRequest("2회차 미팅", "수정된 내용", LocalDateTime.now().plusDays(7))

        // when & then
        assertThatThrownBy { updateTeamScheduleService.execute(userId, teamId, UUID.randomUUID(), request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_SCHEDULE_NOT_FOUND)
    }

    @Test
    fun `deleteSchedule_성공`() {
        // given
        teamScheduleRepository.save(schedule)

        // when
        deleteTeamScheduleService.execute(userId, teamId, scheduleId)

        // then
        assertThat(teamScheduleRepository.findById(scheduleId)).isNull()
    }

    @Test
    fun `deleteSchedule_팀멤버아님_예외발생`() {
        // given
        val otherId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { deleteTeamScheduleService.execute(otherId, teamId, scheduleId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_TEAM_MEMBER)
    }
}

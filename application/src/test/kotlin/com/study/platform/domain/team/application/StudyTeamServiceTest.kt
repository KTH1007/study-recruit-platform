package com.study.platform.domain.team.application

import com.study.platform.domain.apply.event.ApplyApprovedEvent
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.team.dto.response.StudyTeamResponse
import com.study.platform.domain.team.dto.response.TeamMemberResponse
import com.study.platform.domain.team.model.StudyTeam
import com.study.platform.domain.team.model.TeamMember
import com.study.platform.domain.team.model.TeamMemberRole
import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.support.fake.FakeStudyPostRepository
import com.study.platform.support.fake.FakeStudyTeamRepository
import com.study.platform.support.fake.FakeTeamMemberQueryPort
import com.study.platform.support.fake.FakeTeamMemberRepository
import com.study.platform.support.fake.FakeUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.UUID

class StudyTeamServiceTest {

    private lateinit var studyTeamRepository: FakeStudyTeamRepository
    private lateinit var teamMemberRepository: FakeTeamMemberRepository
    private lateinit var studyPostRepository: FakeStudyPostRepository
    private lateinit var userRepository: FakeUserRepository

    private lateinit var createStudyTeamService: CreateStudyTeamService
    private lateinit var findStudyTeamService: FindStudyTeamService
    private lateinit var findTeamMembersService: FindTeamMembersService
    private lateinit var delegateLeaderService: DelegateLeaderService
    private lateinit var removeTeamMemberService: RemoveTeamMemberService
    private lateinit var leaveTeamService: LeaveTeamService

    private lateinit var leaderId: UUID
    private lateinit var memberId: UUID
    private lateinit var postId: UUID
    private lateinit var teamId: UUID
    private lateinit var leader: User
    private lateinit var member: User
    private lateinit var post: StudyPost
    private lateinit var team: StudyTeam
    private lateinit var leaderMember: TeamMember
    private lateinit var normalMember: TeamMember

    @BeforeEach
    fun setUp() {
        studyTeamRepository = FakeStudyTeamRepository()
        teamMemberRepository = FakeTeamMemberRepository()
        studyPostRepository = FakeStudyPostRepository()
        userRepository = FakeUserRepository()

        createStudyTeamService = CreateStudyTeamService(studyTeamRepository, teamMemberRepository, studyPostRepository, userRepository)
        findStudyTeamService = FindStudyTeamService(studyTeamRepository)
        findTeamMembersService = FindTeamMembersService(FakeTeamMemberQueryPort(teamMemberRepository))
        delegateLeaderService = DelegateLeaderService(teamMemberRepository)
        removeTeamMemberService = RemoveTeamMemberService(teamMemberRepository)
        leaveTeamService = LeaveTeamService(studyTeamRepository, teamMemberRepository)

        leaderId = UUID.randomUUID()
        memberId = UUID.randomUUID()
        postId = UUID.randomUUID()
        teamId = UUID.randomUUID()

        leader = User.create("kakao-1", "팀장", "leader@test.com")
        ReflectionTestUtils.setField(leader, "id", leaderId)

        member = User.create("kakao-2", "팀원", "member@test.com")
        ReflectionTestUtils.setField(member, "id", memberId)

        post = StudyPost.create(leader, "스터디 모집", "열심히", "Java", 5, LocalDateTime.now().plusDays(7))
        ReflectionTestUtils.setField(post, "id", postId)

        team = StudyTeam.create(post)
        ReflectionTestUtils.setField(team, "id", teamId)

        leaderMember = TeamMember.createLeader(team, leader)
        ReflectionTestUtils.setField(leaderMember, "id", UUID.randomUUID())

        normalMember = TeamMember.createMember(team, member)
        ReflectionTestUtils.setField(normalMember, "id", UUID.randomUUID())

        studyPostRepository.save(post)
        userRepository.save(leader)
        userRepository.save(member)
    }

    @Test
    fun `createTeam_팀없을때_새팀생성`() {
        // given
        val event = ApplyApprovedEvent(postId, memberId, "스터디 모집")

        // when
        createStudyTeamService.execute(event)

        // then
        assertThat(studyTeamRepository.findByPostId(postId)).isNotNull()
        assertThat(
            teamMemberRepository.findAllByTeamId(
                studyTeamRepository.findByPostId(postId)!!.id!!
            )
        ).hasSize(2)
    }

    @Test
    fun `createTeam_팀있을때_멤버만추가`() {
        // given
        studyTeamRepository.save(team)
        teamMemberRepository.save(leaderMember)
        val event = ApplyApprovedEvent(postId, memberId, "스터디 모집")

        // when
        createStudyTeamService.execute(event)

        // then
        assertThat(teamMemberRepository.findAllByTeamId(teamId)).hasSize(2)
    }

    @Test
    fun `createTeam_이미멤버인경우_추가안함`() {
        // given
        studyTeamRepository.save(team)
        teamMemberRepository.save(leaderMember)
        teamMemberRepository.save(normalMember)
        val event = ApplyApprovedEvent(postId, memberId, "스터디 모집")

        // when
        createStudyTeamService.execute(event)

        // then
        assertThat(teamMemberRepository.findAllByTeamId(teamId)).hasSize(2)
    }

    @Test
    fun `findTeam_성공`() {
        // given
        studyTeamRepository.save(team)

        // when
        val response: StudyTeamResponse = findStudyTeamService.execute(teamId)

        // then
        assertThat(response).isNotNull()
        assertThat(response.name).isEqualTo("스터디 모집")
    }

    @Test
    fun `findTeam_존재하지않음_예외발생`() {
        // given
        val nonExistentId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { findStudyTeamService.execute(nonExistentId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_NOT_FOUND)
    }

    @Test
    fun `findMembers_성공`() {
        // given
        studyTeamRepository.save(team)
        teamMemberRepository.save(leaderMember)
        teamMemberRepository.save(normalMember)

        // when
        val responses: List<TeamMemberResponse> = findTeamMembersService.execute(teamId)

        // then
        assertThat(responses).hasSize(2)
    }

    @Test
    fun `delegateLeader_성공`() {
        // given
        studyTeamRepository.save(team)
        teamMemberRepository.save(leaderMember)
        teamMemberRepository.save(normalMember)

        // when
        delegateLeaderService.execute(leaderId, teamId, memberId)

        // then
        assertThat(leaderMember.role).isEqualTo(TeamMemberRole.MEMBER)
        assertThat(normalMember.role).isEqualTo(TeamMemberRole.LEADER)
    }

    @Test
    fun `delegateLeader_리더아님_예외발생`() {
        // given
        studyTeamRepository.save(team)
        teamMemberRepository.save(leaderMember)
        teamMemberRepository.save(normalMember)

        // when & then
        assertThatThrownBy { delegateLeaderService.execute(memberId, teamId, leaderId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }

    @Test
    fun `removeMember_성공`() {
        // given
        studyTeamRepository.save(team)
        teamMemberRepository.save(leaderMember)
        teamMemberRepository.save(normalMember)

        // when
        removeTeamMemberService.execute(leaderId, teamId, memberId)

        // then
        assertThat(teamMemberRepository.existsByTeamIdAndUserId(teamId, memberId)).isFalse()
    }

    @Test
    fun `removeMember_리더아님_예외발생`() {
        // given
        studyTeamRepository.save(team)
        teamMemberRepository.save(leaderMember)
        teamMemberRepository.save(normalMember)

        // when & then
        assertThatThrownBy { removeTeamMemberService.execute(memberId, teamId, leaderId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }

    @Test
    fun `leaveTeam_일반멤버_탈퇴성공`() {
        // given
        studyTeamRepository.save(team)
        teamMemberRepository.save(leaderMember)
        teamMemberRepository.save(normalMember)

        // when
        leaveTeamService.execute(memberId, teamId)

        // then
        assertThat(teamMemberRepository.existsByTeamIdAndUserId(teamId, memberId)).isFalse()
    }

    @Test
    fun `leaveTeam_리더_마지막멤버_팀삭제`() {
        // given
        studyTeamRepository.save(team)
        teamMemberRepository.save(leaderMember)

        // when
        leaveTeamService.execute(leaderId, teamId)

        // then
        assertThat(studyTeamRepository.findById(teamId)).isNull()
        assertThat(teamMemberRepository.findAllByTeamId(teamId)).isEmpty()
    }

    @Test
    fun `leaveTeam_리더_다른멤버있음_예외발생`() {
        // given
        studyTeamRepository.save(team)
        teamMemberRepository.save(leaderMember)
        teamMemberRepository.save(normalMember)

        // when & then
        assertThatThrownBy { leaveTeamService.execute(leaderId, teamId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LEADER_MUST_DELEGATE)
    }
}

package com.study.platform.domain.team.application;

import com.study.platform.domain.apply.event.ApplyApprovedEvent;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.team.dto.response.StudyTeamResponse;
import com.study.platform.domain.team.dto.response.TeamMemberResponse;
import com.study.platform.domain.team.model.*;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.support.fake.FakeStudyPostRepository;
import com.study.platform.support.fake.FakeStudyTeamRepository;
import com.study.platform.support.fake.FakeTeamMemberRepository;
import com.study.platform.support.fake.FakeUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudyTeamServiceTest {

    private FakeStudyTeamRepository studyTeamRepository;
    private FakeTeamMemberRepository teamMemberRepository;
    private FakeStudyPostRepository studyPostRepository;
    private FakeUserRepository userRepository;
    private StudyTeamService studyTeamService;

    private UUID leaderId;
    private UUID memberId;
    private UUID postId;
    private UUID teamId;
    private User leader;
    private User member;
    private StudyPost post;
    private StudyTeam team;
    private TeamMember leaderMember;
    private TeamMember normalMember;

    @BeforeEach
    void setUp() {
        studyTeamRepository = new FakeStudyTeamRepository();
        teamMemberRepository = new FakeTeamMemberRepository();
        studyPostRepository = new FakeStudyPostRepository();
        userRepository = new FakeUserRepository();
        studyTeamService = new StudyTeamService(
                studyTeamRepository, teamMemberRepository, studyPostRepository, userRepository);

        leaderId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        postId = UUID.randomUUID();
        teamId = UUID.randomUUID();

        leader = User.create("kakao-1", "팀장", "leader@test.com");
        ReflectionTestUtils.setField(leader, "id", leaderId);

        member = User.create("kakao-2", "팀원", "member@test.com");
        ReflectionTestUtils.setField(member, "id", memberId);

        post = StudyPost.create(leader, "스터디 모집", "열심히", "Java", 5, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(post, "id", postId);

        team = StudyTeam.create(post);
        ReflectionTestUtils.setField(team, "id", teamId);

        leaderMember = TeamMember.createLeader(team, leader);
        ReflectionTestUtils.setField(leaderMember, "id", UUID.randomUUID());

        normalMember = TeamMember.createMember(team, member);
        ReflectionTestUtils.setField(normalMember, "id", UUID.randomUUID());

        studyPostRepository.save(post);
        userRepository.save(leader);
        userRepository.save(member);
    }

    @Test
    void createTeam_팀없을때_새팀생성() {
        // given
        ApplyApprovedEvent event = new ApplyApprovedEvent(postId, memberId, "스터디 모집");

        // when
        studyTeamService.createTeam(event);

        // then
        assertThat(studyTeamRepository.findByPostId(postId)).isPresent();
        assertThat(teamMemberRepository.findAllByTeamId(
                studyTeamRepository.findByPostId(postId).get().getId())).hasSize(2);
    }

    @Test
    void createTeam_팀있을때_멤버만추가() {
        // given
        studyTeamRepository.save(team);
        teamMemberRepository.save(leaderMember);
        ApplyApprovedEvent event = new ApplyApprovedEvent(postId, memberId, "스터디 모집");

        // when
        studyTeamService.createTeam(event);

        // then
        assertThat(teamMemberRepository.findAllByTeamId(teamId)).hasSize(2);
    }

    @Test
    void createTeam_이미멤버인경우_추가안함() {
        // given
        studyTeamRepository.save(team);
        teamMemberRepository.save(leaderMember);
        teamMemberRepository.save(normalMember);
        ApplyApprovedEvent event = new ApplyApprovedEvent(postId, memberId, "스터디 모집");

        // when
        studyTeamService.createTeam(event);

        // then
        assertThat(teamMemberRepository.findAllByTeamId(teamId)).hasSize(2);
    }

    @Test
    void findTeam_성공() {
        // given
        studyTeamRepository.save(team);

        // when
        StudyTeamResponse response = studyTeamService.findTeam(teamId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("스터디 모집");
    }

    @Test
    void findTeam_존재하지않음_예외발생() {
        // when & then
        assertThatThrownBy(() -> studyTeamService.findTeam(UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_NOT_FOUND);
    }

    @Test
    void findMembers_성공() {
        // given
        studyTeamRepository.save(team);
        teamMemberRepository.save(leaderMember);
        teamMemberRepository.save(normalMember);

        // when
        List<TeamMemberResponse> responses = studyTeamService.findMembers(teamId);

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    void delegateLeader_성공() {
        // given
        studyTeamRepository.save(team);
        teamMemberRepository.save(leaderMember);
        teamMemberRepository.save(normalMember);

        // when
        studyTeamService.delegateLeader(leaderId, teamId, memberId);

        // then
        assertThat(leaderMember.getRole()).isEqualTo(TeamMemberRole.MEMBER);
        assertThat(normalMember.getRole()).isEqualTo(TeamMemberRole.LEADER);
    }

    @Test
    void delegateLeader_리더아님_예외발생() {
        // given
        studyTeamRepository.save(team);
        teamMemberRepository.save(leaderMember);
        teamMemberRepository.save(normalMember);

        // when & then
        assertThatThrownBy(() -> studyTeamService.delegateLeader(memberId, teamId, leaderId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    void removeMember_성공() {
        // given
        studyTeamRepository.save(team);
        teamMemberRepository.save(leaderMember);
        teamMemberRepository.save(normalMember);

        // when
        studyTeamService.removeMember(leaderId, teamId, memberId);

        // then
        assertThat(teamMemberRepository.existsByTeamIdAndUserId(teamId, memberId)).isFalse();
    }

    @Test
    void removeMember_리더아님_예외발생() {
        // given
        studyTeamRepository.save(team);
        teamMemberRepository.save(leaderMember);
        teamMemberRepository.save(normalMember);

        // when & then
        assertThatThrownBy(() -> studyTeamService.removeMember(memberId, teamId, leaderId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    void leaveTeam_일반멤버_탈퇴성공() {
        // given
        studyTeamRepository.save(team);
        teamMemberRepository.save(leaderMember);
        teamMemberRepository.save(normalMember);

        // when
        studyTeamService.leaveTeam(memberId, teamId);

        // then
        assertThat(teamMemberRepository.existsByTeamIdAndUserId(teamId, memberId)).isFalse();
    }

    @Test
    void leaveTeam_리더_마지막멤버_팀삭제() {
        // given
        studyTeamRepository.save(team);
        teamMemberRepository.save(leaderMember);

        // when
        studyTeamService.leaveTeam(leaderId, teamId);

        // then
        assertThat(studyTeamRepository.findById(teamId)).isEmpty();
        assertThat(teamMemberRepository.findAllByTeamId(teamId)).isEmpty();
    }

    @Test
    void leaveTeam_리더_다른멤버있음_예외발생() {
        // given
        studyTeamRepository.save(team);
        teamMemberRepository.save(leaderMember);
        teamMemberRepository.save(normalMember);

        // when & then
        assertThatThrownBy(() -> studyTeamService.leaveTeam(leaderId, teamId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LEADER_MUST_DELEGATE);
    }
}

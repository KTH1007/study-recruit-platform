package com.study.platform.domain.team.application;

import com.study.platform.domain.apply.event.ApplyApprovedEvent;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.team.dto.response.StudyTeamResponse;
import com.study.platform.domain.team.dto.response.TeamMemberResponse;
import com.study.platform.domain.team.model.*;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class StudyTeamServiceTest {

    @Mock private StudyTeamRepository studyTeamRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private StudyPostRepository studyPostRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
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
        normalMember = TeamMember.createMember(team, member);
    }

    @Test
    void createTeam_팀없을때_새팀생성() {
        // given
        ApplyApprovedEvent event = new ApplyApprovedEvent(postId, memberId, "스터디 모집");
        given(studyTeamRepository.findByPostId(postId)).willReturn(Optional.empty());
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));
        given(studyTeamRepository.save(any())).willReturn(team);
        given(userRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        studyTeamService.createTeam(event);

        // then
        then(studyTeamRepository).should().save(any(StudyTeam.class));
        then(teamMemberRepository).should().save(argThat(m -> m.getRole() == TeamMemberRole.LEADER));
        then(teamMemberRepository).should().save(argThat(m -> m.getRole() == TeamMemberRole.MEMBER));
    }

    @Test
    void createTeam_팀있을때_멤버만추가() {
        // given
        ApplyApprovedEvent event = new ApplyApprovedEvent(postId, memberId, "스터디 모집");
        given(studyTeamRepository.findByPostId(postId)).willReturn(Optional.of(team));
        given(teamMemberRepository.existsByTeamIdAndUserId(teamId, memberId)).willReturn(false);
        given(userRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        studyTeamService.createTeam(event);

        // then
        then(studyTeamRepository).shouldHaveNoMoreInteractions();
        then(teamMemberRepository).should().save(any(TeamMember.class));
    }

    @Test
    void createTeam_이미멤버인경우_추가안함() {
        // given
        ApplyApprovedEvent event = new ApplyApprovedEvent(postId, memberId, "스터디 모집");
        given(studyTeamRepository.findByPostId(postId)).willReturn(Optional.of(team));
        given(teamMemberRepository.existsByTeamIdAndUserId(teamId, memberId)).willReturn(true);

        // when
        studyTeamService.createTeam(event);

        // then
        then(teamMemberRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void findTeam_성공() {
        // given
        given(studyTeamRepository.findById(teamId)).willReturn(Optional.of(team));

        // when
        StudyTeamResponse response = studyTeamService.findTeam(teamId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("스터디 모집");
    }

    @Test
    void findTeam_존재하지않음_예외발생() {
        // given
        given(studyTeamRepository.findById(teamId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyTeamService.findTeam(teamId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TEAM_NOT_FOUND.getMessage());
    }

    @Test
    void findMembers_성공() {
        // given
        given(teamMemberRepository.findAllByTeamId(teamId)).willReturn(List.of(leaderMember, normalMember));

        // when
        List<TeamMemberResponse> responses = studyTeamService.findMembers(teamId);

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    void delegateLeader_성공() {
        // given
        given(teamMemberRepository.findByTeamIdAndUserId(teamId, leaderId)).willReturn(Optional.of(leaderMember));
        given(teamMemberRepository.findByTeamIdAndUserId(teamId, memberId)).willReturn(Optional.of(normalMember));

        // when
        studyTeamService.delegateLeader(leaderId, teamId, memberId);

        // then
        assertThat(leaderMember.getRole()).isEqualTo(TeamMemberRole.MEMBER);
        assertThat(normalMember.getRole()).isEqualTo(TeamMemberRole.LEADER);
    }

    @Test
    void delegateLeader_리더아님_예외발생() {
        // given
        given(teamMemberRepository.findByTeamIdAndUserId(teamId, memberId)).willReturn(Optional.of(normalMember));

        // when & then
        assertThatThrownBy(() -> studyTeamService.delegateLeader(memberId, teamId, leaderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN.getMessage());
    }

    @Test
    void removeMember_성공() {
        // given
        given(teamMemberRepository.findByTeamIdAndUserId(teamId, leaderId)).willReturn(Optional.of(leaderMember));
        given(teamMemberRepository.findByTeamIdAndUserId(teamId, memberId)).willReturn(Optional.of(normalMember));

        // when
        studyTeamService.removeMember(leaderId, teamId, memberId);

        // then
        then(teamMemberRepository).should().delete(normalMember);
    }

    @Test
    void removeMember_리더아님_예외발생() {
        // given
        given(teamMemberRepository.findByTeamIdAndUserId(teamId, memberId)).willReturn(Optional.of(normalMember));

        // when & then
        assertThatThrownBy(() -> studyTeamService.removeMember(memberId, teamId, leaderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN.getMessage());
    }

    @Test
    void leaveTeam_일반멤버_탈퇴성공() {
        // given
        given(teamMemberRepository.findByTeamIdAndUserId(teamId, memberId)).willReturn(Optional.of(normalMember));

        // when
        studyTeamService.leaveTeam(memberId, teamId);

        // then
        then(teamMemberRepository).should().delete(normalMember);
    }

    @Test
    void leaveTeam_리더_마지막멤버_팀삭제() {
        // given
        given(teamMemberRepository.findByTeamIdAndUserId(teamId, leaderId)).willReturn(Optional.of(leaderMember));
        given(teamMemberRepository.countByTeamId(teamId)).willReturn(1L);
        willDoNothing().given(teamMemberRepository).deleteAllByTeamId(teamId);

        // when
        studyTeamService.leaveTeam(leaderId, teamId);

        // then
        then(teamMemberRepository).should().deleteAllByTeamId(teamId);
        then(studyTeamRepository).should().delete(team);
    }

    @Test
    void leaveTeam_리더_다른멤버있음_예외발생() {
        // given
        given(teamMemberRepository.findByTeamIdAndUserId(teamId, leaderId)).willReturn(Optional.of(leaderMember));
        given(teamMemberRepository.countByTeamId(teamId)).willReturn(2L);

        // when & then
        assertThatThrownBy(() -> studyTeamService.leaveTeam(leaderId, teamId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.LEADER_MUST_DELEGATE.getMessage());
    }
}

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyTeamService {

    private final StudyTeamRepository studyTeamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createTeam(ApplyApprovedEvent event) {
        // 이미 팀이 존재하면 멤버만 추가
        if (studyTeamRepository.findByPostId(event.postId()).isPresent()) {
            addMember(event);
            return;
        }

        // 팀이 없으면 새로 생성
        StudyPost post = studyPostRepository.findByIdWithAuthor(event.postId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        StudyTeam team = studyTeamRepository.save(StudyTeam.create(post));

        // 작성자 LEADER로 등록
        teamMemberRepository.save(TeamMember.createLeader(team, post.getAuthor()));

        // 승인된 지원자를 MEMBER로 등록
        User applicant = userRepository.findById(event.applicantId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        teamMemberRepository.save(TeamMember.createMember(team, applicant));
    }

    public StudyTeamResponse findTeam(UUID teamId) {
        StudyTeam team = getTeam(teamId);
        return StudyTeamResponse.from(team);
    }

    public List<TeamMemberResponse> findMembers(UUID teamId) {
        return teamMemberRepository.findAllByTeamId(teamId).stream()
                .map(TeamMemberResponse::from)
                .toList();
    }

    @Transactional
    public void delegateLeader(UUID userId, UUID teamId, UUID targetUserId) {
        TeamMember currentLeader = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND));
        currentLeader.validateIsLeader();

        TeamMember newLeader = teamMemberRepository.findByTeamIdAndUserId(teamId, targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        currentLeader.downgradeToMember();
        newLeader.upgradeToLeader();
    }

    @Transactional
    public void removeMember(UUID userId, UUID teamId, UUID targetUserId) {
        TeamMember currentLeader = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND));
        currentLeader.validateIsLeader();

        TeamMember target = teamMemberRepository.findByTeamIdAndUserId(teamId, targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND));
        teamMemberRepository.delete(target);
    }

    @Transactional
    public void leaveTeam(UUID userId, UUID teamId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        if (member.isLeader()) {
            boolean isLastMember = teamMemberRepository.countByTeamId(teamId) == 1;
            if (isLastMember) {
                teamMemberRepository.deleteAllByTeamId(teamId);
                studyTeamRepository.delete(member.getTeam());
                return;
            }
            throw new CustomException(ErrorCode.LEADER_MUST_DELEGATE);
        }
        teamMemberRepository.delete(member);
    }

    public StudyTeamResponse findTeamByPostId(UUID postId) {
        StudyTeam team = studyTeamRepository.findByPostId(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        return StudyTeamResponse.from(team);
    }

    private void addMember(ApplyApprovedEvent event) {
        StudyTeam team = studyTeamRepository.findByPostId(event.postId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), event.applicantId())) {
            return;
        }
        User applicant = userRepository.findById(event.applicantId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        teamMemberRepository.save(TeamMember.createMember(team, applicant));
    }

    private StudyTeam getTeam(UUID teamId) {
        return studyTeamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
    }
}

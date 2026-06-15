package com.study.platform.domain.team.application;

import com.study.platform.domain.apply.event.ApplyApprovedEvent;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.team.model.*;
import com.study.platform.domain.team.usecase.CreateStudyTeamUseCase;
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

@Service
@RequiredArgsConstructor
public class CreateStudyTeamService implements CreateStudyTeamUseCase {

    private final StudyTeamRepository studyTeamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(ApplyApprovedEvent event) {
        if (studyTeamRepository.findByPostId(event.postId()).isPresent()) {
            addMember(event);
            return;
        }

        StudyPost post = studyPostRepository.findByIdWithAuthor(event.postId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        StudyTeam team = studyTeamRepository.save(StudyTeam.create(post));

        teamMemberRepository.save(TeamMember.createLeader(team, post.getAuthor()));

        User applicant = userRepository.findById(event.applicantId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        teamMemberRepository.save(TeamMember.createMember(team, applicant));
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
}

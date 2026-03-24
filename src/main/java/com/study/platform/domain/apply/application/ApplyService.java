package com.study.platform.domain.apply.application;

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest;
import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.domain.apply.event.ApplyApprovedEvent;
import com.study.platform.domain.apply.event.ApplyReceivedEvent;
import com.study.platform.domain.apply.event.ApplyRejectedEvent;
import com.study.platform.domain.apply.model.Apply;
import com.study.platform.domain.apply.model.ApplyRepository;
import com.study.platform.domain.apply.model.ApplyStatus;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplyService {

    private final ApplyRepository applyRepository;
    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<ApplyResponse> findApplies(UUID userId, UUID postId) {
        StudyPost post = getPostWithAuthor(postId);
        validateAuthor(post, userId);
        return applyRepository.findAllByPostIdWithApplicant(postId).stream()
                .map(ApplyResponse::from)
                .toList();
    }

    @Transactional
    public ApplyResponse apply(UUID userId, UUID postId, ApplyCreateRequest request) {
        StudyPost post = getPostWithAuthor(postId);
        validatePostOpen(post);
        validateNotAuthor(post, userId);
        validateNotDuplicate(postId, userId);

        User applicant = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Apply apply = Apply.create(post, applicant, request.message());
        applyRepository.save(apply);

        eventPublisher.publishEvent(new ApplyReceivedEvent(
                post.getId(),
                post.getAuthor().getId(),
                post.getTitle()
        ));
        return ApplyResponse.from(apply);
    }

    @Transactional
    public void cancel(UUID userId, UUID postId) {
        Apply apply = applyRepository.findByPostIdAndApplicantId(postId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
        applyRepository.delete(apply);
    }

    @Transactional
    public ApplyResponse approve(UUID userId, UUID applyId) {
        Apply apply = getApplyWithPostAndApplicant(applyId);
        validateAuthor(apply.getPost(), userId);
        apply.approve();

        if (isPostFull(apply.getPost())) {
            apply.getPost().markFull();
        }

        eventPublisher.publishEvent(new ApplyApprovedEvent(
                apply.getPost().getId(),
                apply.getApplicant().getId(),
                apply.getPost().getTitle()
        ));
        return ApplyResponse.from(apply);
    }

    @Transactional
    public ApplyResponse reject(UUID userId, UUID applyId) {
        Apply apply = getApplyWithPostAndApplicant(applyId);
        validateAuthor(apply.getPost(), userId);
        apply.reject();

        eventPublisher.publishEvent(new ApplyRejectedEvent(
                apply.getPost().getId(),
                apply.getApplicant().getId(),
                apply.getPost().getTitle()
        ));
        return ApplyResponse.from(apply);
    }

    // postId로 author JOIN FETCH하여 게시글 조회
    private StudyPost getPostWithAuthor(UUID postId) {
        return studyPostRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    // applyId로 post, applicant JOIN FETCH하여 지원 조회
    private Apply getApplyWithPostAndApplicant(UUID applyId) {
        return applyRepository.findByIdWithPostAndApplicant(applyId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
    }

    private void validatePostOpen(StudyPost post) {
        if (!post.isOpen()) {
            throw new CustomException(ErrorCode.POST_CLOSED);
        }
    }

    private void validateAuthor(StudyPost post, UUID userId) {
        if (!post.isAuthor(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    // 게시글 작성자이면 CANNOT_APPLY_OWN_POST 예외 (본인 게시글 지원 방지)
    private void validateNotAuthor(StudyPost post, UUID userId) {
        if (post.isAuthor(userId)) {
            throw new CustomException(ErrorCode.CANNOT_APPLY_OWN_POST);
        }
    }

    // 이미 지원한 이력이 있으면 ALREADY_APPLIED 예외 (중복 지원 방지)
    private void validateNotDuplicate(UUID postId, UUID userId) {
        if (applyRepository.existsByPostIdAndApplicantId(postId, userId)) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED);
        }
    }

    private boolean isPostFull(StudyPost post) {
        long approvedCount = applyRepository.countByPostIdAndStatus(post.getId(), ApplyStatus.APPROVED);
        return approvedCount >= post.getMaxMembers();
    }
}

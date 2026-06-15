package com.study.platform.domain.apply.application;

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest;
import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.domain.apply.model.Apply;
import com.study.platform.domain.apply.model.ApplyRepository;
import com.study.platform.domain.apply.model.ApplyStatus;
import com.study.platform.domain.apply.port.ApplyQueryPort;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.event.DomainEventPublisher;
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
public class ApplyService {

    private final ApplyRepository applyRepository;
    private final ApplyQueryPort applyQueryPort;
    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    public List<ApplyResponse> findApplies(UUID userId, UUID postId) {
        StudyPost post = getPostWithAuthor(postId);
        post.validateAuthor(userId);
        return applyQueryPort.findAllByPostId(postId);
    }

    @Transactional
    public ApplyResponse apply(UUID userId, UUID postId, ApplyCreateRequest request) {
        StudyPost post = studyPostRepository.findByIdWithAuthorForUpdate(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        post.validateOpen();
        post.validateNotAuthor(userId);

        User applicant = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Apply apply = Apply.create(post, applicant, request.message(), eventPublisher);
        applyRepository.save(apply);
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
        // Apply를 락 없이 먼저 조회해서 postId 획득
        Apply applyInfo = applyRepository.findByIdWithPostAndApplicant(applyId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        // StudyPost 먼저 락
        StudyPost post = studyPostRepository.findByIdWithAuthorForUpdate(applyInfo.getPost().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        post.validateAuthor(userId);

        // Apply 락
        Apply apply = applyRepository.findByIdWithPostAndApplicantForUpdate(applyId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
        apply.approve(eventPublisher);

        long approvedCount = applyRepository.countByPostIdAndStatus(post.getId(), ApplyStatus.APPROVED);
        post.markFullIfNeeded(approvedCount);

        return ApplyResponse.from(apply);
    }

    @Transactional
    public ApplyResponse reject(UUID userId, UUID applyId) {
        Apply apply = applyRepository.findByIdWithPostAndApplicantForUpdate(applyId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
        StudyPost post = apply.getPost();
        post.validateAuthor(userId);
        apply.reject(eventPublisher);
        return ApplyResponse.from(apply);
    }

    // postId로 author JOIN FETCH하여 게시글 조회
    private StudyPost getPostWithAuthor(UUID postId) {
        return studyPostRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }


}

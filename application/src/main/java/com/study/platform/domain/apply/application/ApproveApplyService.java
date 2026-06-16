package com.study.platform.domain.apply.application;

import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.domain.apply.model.Apply;
import com.study.platform.domain.apply.model.ApplyRepository;
import com.study.platform.domain.apply.model.ApplyStatus;
import com.study.platform.domain.apply.usecase.ApproveApplyUseCase;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.global.event.DomainEventPublisher;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApproveApplyService implements ApproveApplyUseCase {

    private final ApplyRepository applyRepository;
    private final StudyPostRepository studyPostRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public ApplyResponse execute(UUID userId, UUID applyId) {
        Apply applyInfo = applyRepository.findByIdWithPostAndApplicant(applyId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        StudyPost post = studyPostRepository.findByIdWithAuthorForUpdate(applyInfo.getPost().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        post.validateAuthor(userId);

        Apply apply = applyRepository.findByIdWithPostAndApplicantForUpdate(applyId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
        apply.approve(eventPublisher);
        applyRepository.save(apply);

        long approvedCount = applyRepository.countByPostIdAndStatus(post.getId(), ApplyStatus.APPROVED);
        post.markFullIfNeeded(approvedCount);

        return ApplyResponse.from(apply);
    }
}

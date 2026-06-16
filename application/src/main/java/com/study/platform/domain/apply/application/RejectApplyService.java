package com.study.platform.domain.apply.application;

import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.domain.apply.model.Apply;
import com.study.platform.domain.apply.model.ApplyRepository;
import com.study.platform.domain.apply.usecase.RejectApplyUseCase;
import com.study.platform.global.event.DomainEventPublisher;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RejectApplyService implements RejectApplyUseCase {

    private final ApplyRepository applyRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public ApplyResponse execute(UUID userId, UUID applyId) {
        Apply apply = applyRepository.findByIdWithPostAndApplicantForUpdate(applyId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
        apply.getPost().validateAuthor(userId);
        apply.reject(eventPublisher);
        applyRepository.save(apply);
        return ApplyResponse.from(apply);
    }
}

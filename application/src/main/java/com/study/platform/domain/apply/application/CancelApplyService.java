package com.study.platform.domain.apply.application;

import com.study.platform.domain.apply.model.Apply;
import com.study.platform.domain.apply.model.ApplyRepository;
import com.study.platform.domain.apply.usecase.CancelApplyUseCase;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelApplyService implements CancelApplyUseCase {

    private final ApplyRepository applyRepository;

    @Override
    @Transactional
    public void execute(UUID userId, UUID postId) {
        Apply apply = applyRepository.findByPostIdAndApplicantId(postId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
        applyRepository.delete(apply);
    }
}

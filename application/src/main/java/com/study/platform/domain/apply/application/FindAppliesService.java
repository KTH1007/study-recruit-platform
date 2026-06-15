package com.study.platform.domain.apply.application;

import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.domain.apply.port.ApplyQueryPort;
import com.study.platform.domain.apply.usecase.FindAppliesUseCase;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
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
public class FindAppliesService implements FindAppliesUseCase {

    private final ApplyQueryPort applyQueryPort;
    private final StudyPostRepository studyPostRepository;

    @Override
    public List<ApplyResponse> execute(UUID userId, UUID postId) {
        StudyPost post = studyPostRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        post.validateAuthor(userId);
        return applyQueryPort.findAllByPostId(postId);
    }
}

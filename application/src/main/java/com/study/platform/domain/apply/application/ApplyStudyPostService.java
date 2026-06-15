package com.study.platform.domain.apply.application;

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest;
import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.domain.apply.model.Apply;
import com.study.platform.domain.apply.model.ApplyRepository;
import com.study.platform.domain.apply.usecase.ApplyStudyPostUseCase;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplyStudyPostService implements ApplyStudyPostUseCase {

    private final ApplyRepository applyRepository;
    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public ApplyResponse execute(UUID userId, UUID postId, ApplyCreateRequest request) {
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
}

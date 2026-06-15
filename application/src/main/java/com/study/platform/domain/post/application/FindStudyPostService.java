package com.study.platform.domain.post.application;

import com.study.platform.domain.post.dto.response.StudyPostResponse;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.post.usecase.FindStudyPostUseCase;
import com.study.platform.global.constant.CacheConstants;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindStudyPostService implements FindStudyPostUseCase {

    private final StudyPostRepository studyPostRepository;

    @Override
    @Cacheable(cacheNames = CacheConstants.POST_CACHE, key = "#postId")
    public StudyPostResponse execute(UUID postId) {
        StudyPost post = studyPostRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return StudyPostResponse.from(post);
    }
}

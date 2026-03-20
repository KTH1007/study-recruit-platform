package com.study.platform.domain.post.application;

import com.study.platform.domain.post.dto.request.StudyPostCreateRequest;
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest;
import com.study.platform.domain.post.dto.response.StudyPostResponse;
import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyPostService {

    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;

    public Page<StudyPostSummaryResponse> findPosts(String techStack, StudyPostStatus status, Pageable pageable) {
        return studyPostRepository.findAllWithFilter(techStack, status, pageable)
                .map(StudyPostSummaryResponse::from);
    }

    public StudyPostResponse findPost(UUID postId) {
        return StudyPostResponse.from(getPostWithAuthor(postId));
    }

    @Transactional
    public StudyPostResponse createPost(UUID userId, StudyPostCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        StudyPost post = request.toEntity(user);
        return StudyPostResponse.from(studyPostRepository.save(post));
    }

    @Transactional
    public StudyPostResponse updatePost(UUID userId, UUID postId, StudyPostUpdateRequest request) {
        StudyPost post = getPostWithAuthor(postId);
        validateAuthor(post, userId);
        post.update(request.title(), request.description(), request.techStack(),
                request.maxMembers(), request.deadline());
        return StudyPostResponse.from(post);
    }

    @Transactional
    public void deletePost(UUID userId, UUID postId) {
        StudyPost post = getPostWithAuthor(postId);
        validateAuthor(post, userId);
        studyPostRepository.delete(post);
    }

    @Transactional
    public StudyPostResponse closePost(UUID userId, UUID postId) {
        StudyPost post = getPostWithAuthor(postId);
        validateAuthor(post, userId);
        post.close();
        return StudyPostResponse.from(post);
    }

    private StudyPost getPostWithAuthor(UUID postId) {
        return studyPostRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void validateAuthor(StudyPost post, UUID userId) {
        if (!post.isAuthor(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}

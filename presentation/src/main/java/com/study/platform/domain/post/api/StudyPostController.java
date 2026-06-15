package com.study.platform.domain.post.api;

import com.study.platform.domain.post.api.doc.StudyPostControllerDoc;
import com.study.platform.domain.post.application.PostSearchService;
import com.study.platform.domain.post.dto.request.StudyPostCreateRequest;
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest;
import com.study.platform.domain.post.dto.response.StudyPostResponse;
import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.domain.post.usecase.*;
import com.study.platform.domain.team.dto.response.StudyTeamResponse;
import com.study.platform.domain.team.usecase.FindStudyTeamUseCase;
import com.study.platform.global.idempotency.Idempotent;
import com.study.platform.global.ratelimit.RateLimit;
import com.study.platform.global.response.ApiResponse;
import com.study.platform.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class StudyPostController implements StudyPostControllerDoc {

    private final FindStudyPostsUseCase findStudyPostsUseCase;
    private final FindStudyPostUseCase findStudyPostUseCase;
    private final CreateStudyPostUseCase createStudyPostUseCase;
    private final UpdateStudyPostUseCase updateStudyPostUseCase;
    private final DeleteStudyPostUseCase deleteStudyPostUseCase;
    private final CloseStudyPostUseCase closeStudyPostUseCase;
    private final FindStudyTeamUseCase findStudyTeamUseCase;
    private final PostSearchService postSearchService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<StudyPostSummaryResponse>>> findPosts(
            @RequestParam(required = false) String techStack,
            @RequestParam(required = false) StudyPostStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(SuccessCode.POST_LIST, findStudyPostsUseCase.execute(techStack, status, pageable));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<StudyPostResponse>> findPost(
            @PathVariable UUID postId) {
        return ApiResponse.success(SuccessCode.POST_DETAIL, findStudyPostUseCase.execute(postId));
    }

    @Idempotent
    @RateLimit(limit = 3, windowSeconds = 60)
    @PostMapping
    public ResponseEntity<ApiResponse<StudyPostResponse>> createPost(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody StudyPostCreateRequest request) {
        return ApiResponse.success(SuccessCode.POST_CREATED, createStudyPostUseCase.execute(userId, request));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<StudyPostResponse>> updatePost(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId,
            @Valid @RequestBody StudyPostUpdateRequest request) {
        return ApiResponse.success(SuccessCode.POST_UPDATED, updateStudyPostUseCase.execute(userId, postId, request));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId) {
        deleteStudyPostUseCase.execute(userId, postId);
        return ApiResponse.success(SuccessCode.POST_DELETED, null);
    }

    @PatchMapping("/{postId}/close")
    public ResponseEntity<ApiResponse<StudyPostResponse>> closePost(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId) {
        return ApiResponse.success(SuccessCode.POST_UPDATED, closeStudyPostUseCase.execute(userId, postId));
    }

    @GetMapping("/{postId}/team")
    public ResponseEntity<ApiResponse<StudyTeamResponse>> findTeamByPostId(
            @PathVariable UUID postId) {
        return ApiResponse.success(SuccessCode.TEAM_FOUND, findStudyTeamUseCase.executeByPostId(postId));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<StudyPostSummaryResponse>>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String techStack,
            @RequestParam(required = false) StudyPostStatus status,
            @RequestParam(required = false, defaultValue = "0") int maxMembers,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(SuccessCode.POST_LIST,
                postSearchService.search(keyword, techStack, status, maxMembers, pageable)
                        .map(StudyPostSummaryResponse::from));
    }
}

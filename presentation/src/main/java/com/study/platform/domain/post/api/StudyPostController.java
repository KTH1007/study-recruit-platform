package com.study.platform.domain.post.api;

import com.study.platform.domain.post.api.doc.StudyPostControllerDoc;
import com.study.platform.domain.post.application.PostSearchService;
import com.study.platform.domain.post.application.StudyPostService;
import com.study.platform.domain.post.dto.request.StudyPostCreateRequest;
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest;
import com.study.platform.domain.post.dto.response.StudyPostResponse;
import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.domain.team.application.StudyTeamService;
import com.study.platform.domain.team.dto.response.StudyTeamResponse;
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

    private final StudyPostService studyPostService;
    private final StudyTeamService studyTeamService;
    private final PostSearchService postSearchService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<StudyPostSummaryResponse>>> findPosts(
            @RequestParam(required = false) String techStack,
            @RequestParam(required = false)StudyPostStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(SuccessCode.POST_LIST, studyPostService.findPosts(techStack, status, pageable));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<StudyPostResponse>> findPost(
            @PathVariable UUID postId) {
        return ApiResponse.success(SuccessCode.POST_DETAIL, studyPostService.findPost(postId));
    }

    @Idempotent
    @RateLimit(limit = 3, windowSeconds = 60)
    @PostMapping
    public ResponseEntity<ApiResponse<StudyPostResponse>> createPost(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody StudyPostCreateRequest request) {
        return ApiResponse.success(SuccessCode.POST_CREATED, studyPostService.createPost(userId, request));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<StudyPostResponse>> updatePost(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId,
            @Valid @RequestBody StudyPostUpdateRequest request) {
        return ApiResponse.success(SuccessCode.POST_UPDATED, studyPostService.updatePost(userId, postId, request));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId) {
        studyPostService.deletePost(userId, postId);
        return ApiResponse.success(SuccessCode.POST_DELETED, null);
    }

    @PatchMapping("/{postId}/close")
    public ResponseEntity<ApiResponse<StudyPostResponse>> closePost(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId) {
        return ApiResponse.success(SuccessCode.POST_UPDATED, studyPostService.closePost(userId, postId));
    }

    @GetMapping("/{postId}/team")
    public ResponseEntity<ApiResponse<StudyTeamResponse>> findTeamByPostId(
            @PathVariable UUID postId) {
        return ApiResponse.success(SuccessCode.TEAM_FOUND, studyTeamService.findTeamByPostId(postId));
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

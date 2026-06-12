package com.study.platform.domain.comment.api;

import com.study.platform.domain.comment.api.doc.CommentControllerDoc;
import com.study.platform.domain.comment.application.CommentService;
import com.study.platform.domain.comment.dto.request.CommentCreateRequest;
import com.study.platform.domain.comment.dto.request.CommentUpdateRequest;
import com.study.platform.domain.comment.dto.response.CommentResponse;
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
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController implements CommentControllerDoc {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> findComments(
            @PathVariable UUID postId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)Pageable pageable) {
        return ApiResponse.success(SuccessCode.COMMENT_LIST, commentService.findComments(postId, pageable));
    }

    @Idempotent
    @RateLimit(limit = 10, windowSeconds = 60)
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CommentCreateRequest request) {
        return ApiResponse.success(SuccessCode.COMMENT_CREATED, commentService.createComment(userId, postId, request));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CommentUpdateRequest request) {
        return ApiResponse.success(SuccessCode.COMMENT_UPDATED, commentService.updateComment(userId, commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal UUID userId) {
        commentService.deleteComment(userId, commentId);
        return ApiResponse.success(SuccessCode.COMMENT_DELETED, null);
    }
}

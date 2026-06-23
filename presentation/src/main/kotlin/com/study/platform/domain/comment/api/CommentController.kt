package com.study.platform.domain.comment.api

import com.study.platform.domain.comment.api.doc.CommentControllerDoc
import com.study.platform.domain.comment.dto.request.CommentCreateRequest
import com.study.platform.domain.comment.dto.request.CommentUpdateRequest
import com.study.platform.domain.comment.dto.response.CommentResponse
import com.study.platform.domain.comment.usecase.CreateCommentUseCase
import com.study.platform.domain.comment.usecase.DeleteCommentUseCase
import com.study.platform.domain.comment.usecase.FindCommentsUseCase
import com.study.platform.domain.comment.usecase.UpdateCommentUseCase
import com.study.platform.global.idempotency.Idempotent
import com.study.platform.global.ratelimit.RateLimit
import com.study.platform.global.response.ApiResponse
import com.study.platform.global.response.SuccessCode
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/posts/{postId}/comments")
class CommentController(
    private val findCommentsUseCase: FindCommentsUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val updateCommentUseCase: UpdateCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase
) : CommentControllerDoc {

    @GetMapping
    override fun findComments(
        @PathVariable postId: UUID,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.ASC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<CommentResponse>>> {
        return ApiResponse.success(SuccessCode.COMMENT_LIST, findCommentsUseCase.execute(postId, pageable))
    }

    @Idempotent
    @RateLimit(limit = 10, windowSeconds = 60)
    @PostMapping
    override fun createComment(
        @PathVariable postId: UUID,
        @AuthenticationPrincipal userId: UUID,
        @Valid @RequestBody request: CommentCreateRequest
    ): ResponseEntity<ApiResponse<CommentResponse>> {
        return ApiResponse.success(SuccessCode.COMMENT_CREATED, createCommentUseCase.execute(userId, postId, request))
    }

    @PatchMapping("/{commentId}")
    override fun updateComment(
        @PathVariable commentId: UUID,
        @AuthenticationPrincipal userId: UUID,
        @Valid @RequestBody request: CommentUpdateRequest
    ): ResponseEntity<ApiResponse<CommentResponse>> {
        return ApiResponse.success(SuccessCode.COMMENT_UPDATED, updateCommentUseCase.execute(userId, commentId, request))
    }

    @DeleteMapping("/{commentId}")
    override fun deleteComment(
        @PathVariable commentId: UUID,
        @AuthenticationPrincipal userId: UUID
    ): ResponseEntity<ApiResponse<Void>> {
        deleteCommentUseCase.execute(userId, commentId)
        return ApiResponse.success(SuccessCode.COMMENT_DELETED)
    }
}

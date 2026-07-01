package com.study.platform.domain.apply.api

import com.study.platform.domain.apply.api.doc.ApplyControllerDoc
import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.usecase.*
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
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api")
class ApplyController(
    private val findAppliesUseCase: FindAppliesUseCase,
    private val applyStudyPostUseCase: ApplyStudyPostUseCase,
    private val cancelApplyUseCase: CancelApplyUseCase,
    private val approveApplyUseCase: ApproveApplyUseCase,
    private val rejectApplyUseCase: RejectApplyUseCase
) : ApplyControllerDoc {

    @GetMapping("/posts/{postId}/applies")
    override fun findApplies(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable postId: UUID,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<ApplyResponse>>> =
        ApiResponse.success(SuccessCode.APPLY_LIST, findAppliesUseCase.execute(userId, postId, pageable))

    @Idempotent
    @RateLimit(limit = 5, windowSeconds = 60)
    @PostMapping("/posts/{postId}/applies")
    override fun apply(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable postId: UUID,
        @Valid @RequestBody request: ApplyCreateRequest
    ): ResponseEntity<ApiResponse<ApplyResponse>> =
        ApiResponse.success(SuccessCode.APPLY_CREATED, applyStudyPostUseCase.execute(userId, postId, request))

    @DeleteMapping("/posts/{postId}/applies")
    override fun cancel(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable postId: UUID
    ): ResponseEntity<ApiResponse<Void>> {
        cancelApplyUseCase.execute(userId, postId)
        return ApiResponse.success(SuccessCode.APPLY_CANCELED)
    }

    @RateLimit(limit = 5, windowSeconds = 60)
    @PatchMapping("/applies/{applyId}/approve")
    override fun approve(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable applyId: UUID
    ): ResponseEntity<ApiResponse<ApplyResponse>> =
        ApiResponse.success(SuccessCode.APPLY_APPROVED, approveApplyUseCase.execute(userId, applyId))

    @RateLimit(limit = 5, windowSeconds = 60)
    @PatchMapping("/applies/{applyId}/reject")
    override fun reject(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable applyId: UUID
    ): ResponseEntity<ApiResponse<ApplyResponse>> =
        ApiResponse.success(SuccessCode.APPLY_REJECTED, rejectApplyUseCase.execute(userId, applyId))
}

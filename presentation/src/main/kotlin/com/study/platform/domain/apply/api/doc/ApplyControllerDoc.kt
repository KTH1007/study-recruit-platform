package com.study.platform.domain.apply.api.doc

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import java.util.UUID

@Tag(name = "Apply", description = "스터디 지원 API")
interface ApplyControllerDoc {

    @Operation(summary = "지원 목록 조회", description = "방장이 본인 게시글의 지원 목록을 조회합니다.")
    fun findApplies(
        @Parameter(hidden = true) userId: UUID,
        @Parameter(description = "게시글 ID") postId: UUID,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<ApplyResponse>>>

    @Operation(summary = "지원", description = "스터디 모집글에 지원합니다.")
    fun apply(
        @Parameter(hidden = true) userId: UUID,
        @Parameter(description = "게시글 ID") postId: UUID,
        @Valid request: ApplyCreateRequest
    ): ResponseEntity<ApiResponse<ApplyResponse>>

    @Operation(summary = "지원 취소", description = "본인의 지원을 취소합니다.")
    fun cancel(
        @Parameter(hidden = true) userId: UUID,
        @Parameter(description = "게시글 ID") postId: UUID
    ): ResponseEntity<ApiResponse<Void>>

    @Operation(summary = "지원 승인", description = "방장이 지원을 승인합니다.")
    fun approve(
        @Parameter(hidden = true) userId: UUID,
        @Parameter(description = "지원 ID") applyId: UUID
    ): ResponseEntity<ApiResponse<ApplyResponse>>

    @Operation(summary = "지원 거절", description = "방장이 지원을 거절합니다.")
    fun reject(
        @Parameter(hidden = true) userId: UUID,
        @Parameter(description = "지원 ID") applyId: UUID
    ): ResponseEntity<ApiResponse<ApplyResponse>>
}

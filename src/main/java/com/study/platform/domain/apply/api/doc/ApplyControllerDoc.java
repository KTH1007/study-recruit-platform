package com.study.platform.domain.apply.api.doc;

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest;
import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "Apply", description = "스터디 지원 API")
public interface ApplyControllerDoc {

    @Operation(summary = "지원 목록 조회", description = "방장이 본인 게시글의 지원 목록을 조회합니다.")
    ResponseEntity<ApiResponse<List<ApplyResponse>>> findApplies(
            @Parameter(hidden = true) UUID userId,
            @Parameter(description = "게시글 ID") UUID postId);

    @Operation(summary = "지원", description = "스터디 모집글에 지원합니다.")
    ResponseEntity<ApiResponse<ApplyResponse>> apply(
            @Parameter(hidden = true) UUID userId,
            @Parameter(description = "게시글 ID") UUID postId,
            @Valid ApplyCreateRequest request);

    @Operation(summary = "지원 취소", description = "본인의 지원을 취소합니다.")
    ResponseEntity<ApiResponse<Void>> cancel(
            @Parameter(hidden = true) UUID userId,
            @Parameter(description = "게시글 ID") UUID postId);

    @Operation(summary = "지원 승인", description = "방장이 지원을 승인합니다.")
    ResponseEntity<ApiResponse<ApplyResponse>> approve(
            @Parameter(hidden = true) UUID userId,
            @Parameter(description = "지원 ID") UUID applyId);

    @Operation(summary = "지원 거절", description = "방장이 지원을 거절합니다.")
    ResponseEntity<ApiResponse<ApplyResponse>> reject(
            @Parameter(hidden = true) UUID userId,
            @Parameter(description = "지원 ID") UUID applyId);
}

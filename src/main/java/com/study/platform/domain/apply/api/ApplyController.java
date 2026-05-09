package com.study.platform.domain.apply.api;

import com.study.platform.domain.apply.api.doc.ApplyControllerDoc;
import com.study.platform.domain.apply.application.ApplyService;
import com.study.platform.domain.apply.dto.request.ApplyCreateRequest;
import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.global.idempotency.Idempotent;
import com.study.platform.global.ratelimit.RateLimit;
import com.study.platform.global.response.ApiResponse;
import com.study.platform.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApplyController implements ApplyControllerDoc {

    private final ApplyService applyService;

    @GetMapping("/posts/{postId}/applies")
    public ResponseEntity<ApiResponse<List<ApplyResponse>>> findApplies(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId) {
        return ApiResponse.success(SuccessCode.APPLY_LIST, applyService.findApplies(userId, postId));
    }

    @Idempotent
    @RateLimit(limit = 5, windowSeconds = 60)
    @PostMapping("/posts/{postId}/applies")
    public ResponseEntity<ApiResponse<ApplyResponse>> apply(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId,
            @Valid @RequestBody ApplyCreateRequest request) {
        return ApiResponse.success(SuccessCode.APPLY_CREATED, applyService.apply(userId, postId, request));
    }

    @DeleteMapping("/posts/{postId}/applies")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId) {
        applyService.cancel(userId, postId);
        return ApiResponse.success(SuccessCode.APPLY_CANCELED, null);
    }

    @RateLimit(limit = 5, windowSeconds = 60)
    @PatchMapping("/applies/{applyId}/approve")
    public ResponseEntity<ApiResponse<ApplyResponse>> approve(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID applyId) {
        return ApiResponse.success(SuccessCode.APPLY_APPROVED, applyService.approve(userId, applyId));
    }

    @RateLimit(limit = 5, windowSeconds = 60)
    @PatchMapping("/applies/{applyId}/reject")
    public ResponseEntity<ApiResponse<ApplyResponse>> reject(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID applyId) {
        return ApiResponse.success(SuccessCode.APPLY_REJECTED, applyService.reject(userId, applyId));
    }
}

package com.study.platform.domain.post.api.doc;

import com.study.platform.domain.post.dto.request.StudyPostCreateRequest;
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest;
import com.study.platform.domain.post.dto.response.StudyPostResponse;
import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "StudyPost", description = "스터디 모집 게시글 API")
public interface StudyPostControllerDoc {

    @Operation(summary = "게시글 목록 조회", description = "기술스택, 상태로 필터링하여 게시글 목록을 조회합니다.")
    @Parameters({
            @Parameter(name = "techStack", description = "기술스택 필터 (예: Java)"),
            @Parameter(name = "status", description = "모집 상태 필터 (OPEN: 모집 중, CLOSED: 모집 마감, FULL: 정원 초과)"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "10"),
            @Parameter(name = "sort", description = "정렬 기준", example = "createdAt,desc")
    })
    ResponseEntity<ApiResponse<Page<StudyPostSummaryResponse>>> findPosts(
            @Parameter(hidden = true) String techStack,
            @Parameter(hidden = true) StudyPostStatus status,
            @Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "게시글 단건 조회")
    ResponseEntity<ApiResponse<StudyPostResponse>> findPost(
            @Parameter(description = "게시글 ID") UUID postId);

    @Operation(summary = "게시글 작성")
    ResponseEntity<ApiResponse<StudyPostResponse>> createPost(
            @Parameter(hidden = true) UUID userId,
            @Valid StudyPostCreateRequest request);

    @Operation(summary = "게시글 수정")
    ResponseEntity<ApiResponse<StudyPostResponse>> updatePost(
            @Parameter(hidden = true) UUID userId,
            @Parameter(description = "게시글 ID") UUID postId,
            @Valid StudyPostUpdateRequest request);

    @Operation(summary = "게시글 삭제")
    ResponseEntity<Void> deletePost(
            @Parameter(hidden = true) UUID userId,
            @Parameter(description = "게시글 ID") UUID postId);

    @Operation(summary = "모집 마감", description = "방장이 직접 모집을 마감합니다.")
    ResponseEntity<ApiResponse<StudyPostResponse>> closePost(
            @Parameter(hidden = true) UUID userId,
            @Parameter(description = "게시글 ID") UUID postId);
}
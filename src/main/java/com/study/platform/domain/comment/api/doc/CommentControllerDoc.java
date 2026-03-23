package com.study.platform.domain.comment.api.doc;

import com.study.platform.domain.comment.dto.request.CommentCreateRequest;
import com.study.platform.domain.comment.dto.request.CommentUpdateRequest;
import com.study.platform.domain.comment.dto.response.CommentResponse;
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

@Tag(name = "Comment", description = "댓글 API")
public interface CommentControllerDoc {

    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 페이징으로 조회합니다.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "20"),
            @Parameter(name = "sort", description = "정렬 기준 (createdAt,asc / createdAt,desc)", example = "createdAt,asc")
    })
    ResponseEntity<ApiResponse<Page<CommentResponse>>> findComments(UUID postId, @Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다. @닉네임으로 멘션 가능합니다.")
    ResponseEntity<ApiResponse<CommentResponse>> createComment(UUID postId, UUID userId, @Valid CommentCreateRequest request);

    @Operation(summary = "댓글 수정")
    ResponseEntity<ApiResponse<CommentResponse>> updateComment(UUID commentId, UUID userId, @Valid CommentUpdateRequest request);

    @Operation(summary = "댓글 삭제")
    ResponseEntity<ApiResponse<Void>> deleteComment(UUID commentId, UUID userId);
}

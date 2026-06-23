package com.study.platform.domain.post.api.doc

import com.study.platform.domain.post.dto.request.StudyPostCreateRequest
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest
import com.study.platform.domain.post.dto.response.StudyPostResponse
import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse
import com.study.platform.domain.post.model.StudyPostStatus
import com.study.platform.domain.team.dto.response.StudyTeamResponse
import com.study.platform.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import java.util.UUID

@Tag(name = "StudyPost", description = "스터디 모집 게시글 API")
interface StudyPostControllerDoc {

    @Operation(summary = "게시글 목록 조회", description = "기술스택, 상태로 필터링하여 게시글 목록을 조회합니다.")
    @Parameters(
        Parameter(name = "techStack", description = "기술스택 필터 (예: Java)"),
        Parameter(name = "status", description = "모집 상태 필터 (OPEN: 모집 중, CLOSED: 모집 마감, FULL: 정원 초과)"),
        Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
        Parameter(name = "size", description = "페이지 크기", example = "10"),
        Parameter(name = "sort", description = "정렬 기준", example = "createdAt,desc")
    )
    fun findPosts(
        @Parameter(hidden = true) techStack: String?,
        @Parameter(hidden = true) status: StudyPostStatus?,
        @Parameter(hidden = true) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<StudyPostSummaryResponse>>>

    @Operation(summary = "게시글 단건 조회")
    fun findPost(@Parameter(description = "게시글 ID") postId: UUID): ResponseEntity<ApiResponse<StudyPostResponse>>

    @Operation(summary = "게시글 작성")
    fun createPost(
        @Parameter(hidden = true) userId: UUID,
        @Valid request: StudyPostCreateRequest
    ): ResponseEntity<ApiResponse<StudyPostResponse>>

    @Operation(summary = "게시글 수정")
    fun updatePost(
        @Parameter(hidden = true) userId: UUID,
        @Parameter(description = "게시글 ID") postId: UUID,
        @Valid request: StudyPostUpdateRequest
    ): ResponseEntity<ApiResponse<StudyPostResponse>>

    @Operation(summary = "게시글 삭제")
    fun deletePost(
        @Parameter(hidden = true) userId: UUID,
        @Parameter(description = "게시글 ID") postId: UUID
    ): ResponseEntity<ApiResponse<Void>>

    @Operation(summary = "모집 마감", description = "방장이 직접 모집을 마감합니다.")
    fun closePost(
        @Parameter(hidden = true) userId: UUID,
        @Parameter(description = "게시글 ID") postId: UUID
    ): ResponseEntity<ApiResponse<StudyPostResponse>>

    @Operation(summary = "게시글 팀 조회", description = "게시글에 연결된 스터디팀을 조회합니다.")
    fun findTeamByPostId(@Parameter(description = "게시글 ID") postId: UUID): ResponseEntity<ApiResponse<StudyTeamResponse>>

    @Operation(summary = "게시글 검색", description = "Elasticsearch 기반 복합 조건 검색. 키워드는 nori 형태소 분석기로 처리됩니다.")
    @Parameters(
        Parameter(name = "keyword", description = "검색 키워드 (제목, 내용 대상)"),
        Parameter(name = "techStack", description = "기술스택 필터 (예: Java)"),
        Parameter(name = "status", description = "모집 상태 필터"),
        Parameter(name = "maxMembers", description = "최대 인원 이하 필터 (0이면 미적용)"),
        Parameter(name = "page", description = "페이지 번호", example = "0"),
        Parameter(name = "size", description = "페이지 크기", example = "10")
    )
    fun searchPosts(
        @Parameter(hidden = true) keyword: String?,
        @Parameter(hidden = true) techStack: String?,
        @Parameter(hidden = true) status: StudyPostStatus?,
        @Parameter(hidden = true) maxMembers: Int,
        @Parameter(hidden = true) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<StudyPostSummaryResponse>>>
}

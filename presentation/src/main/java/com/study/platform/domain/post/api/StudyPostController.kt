package com.study.platform.domain.post.api

import com.study.platform.domain.post.api.doc.StudyPostControllerDoc
import com.study.platform.domain.post.application.PostSearchService
import com.study.platform.domain.post.dto.request.StudyPostCreateRequest
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest
import com.study.platform.domain.post.dto.response.StudyPostResponse
import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse
import com.study.platform.domain.post.model.StudyPostStatus
import com.study.platform.domain.post.usecase.CloseStudyPostUseCase
import com.study.platform.domain.post.usecase.CreateStudyPostUseCase
import com.study.platform.domain.post.usecase.DeleteStudyPostUseCase
import com.study.platform.domain.post.usecase.FindStudyPostUseCase
import com.study.platform.domain.post.usecase.FindStudyPostsUseCase
import com.study.platform.domain.post.usecase.UpdateStudyPostUseCase
import com.study.platform.domain.team.dto.response.StudyTeamResponse
import com.study.platform.domain.team.usecase.FindStudyTeamUseCase
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/posts")
class StudyPostController(
    private val findStudyPostsUseCase: FindStudyPostsUseCase,
    private val findStudyPostUseCase: FindStudyPostUseCase,
    private val createStudyPostUseCase: CreateStudyPostUseCase,
    private val updateStudyPostUseCase: UpdateStudyPostUseCase,
    private val deleteStudyPostUseCase: DeleteStudyPostUseCase,
    private val closeStudyPostUseCase: CloseStudyPostUseCase,
    private val findStudyTeamUseCase: FindStudyTeamUseCase,
    private val postSearchService: PostSearchService
) : StudyPostControllerDoc {

    @GetMapping
    override fun findPosts(
        @RequestParam(required = false) techStack: String?,
        @RequestParam(required = false) status: StudyPostStatus?,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<StudyPostSummaryResponse>>> {
        return ApiResponse.success(SuccessCode.POST_LIST, findStudyPostsUseCase.execute(techStack, status, pageable))
    }

    @GetMapping("/{postId}")
    override fun findPost(@PathVariable postId: UUID): ResponseEntity<ApiResponse<StudyPostResponse>> {
        return ApiResponse.success(SuccessCode.POST_DETAIL, findStudyPostUseCase.execute(postId))
    }

    @Idempotent
    @RateLimit(limit = 3, windowSeconds = 60)
    @PostMapping
    override fun createPost(
        @AuthenticationPrincipal userId: UUID,
        @Valid @RequestBody request: StudyPostCreateRequest
    ): ResponseEntity<ApiResponse<StudyPostResponse>> {
        return ApiResponse.success(SuccessCode.POST_CREATED, createStudyPostUseCase.execute(userId, request))
    }

    @PatchMapping("/{postId}")
    override fun updatePost(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable postId: UUID,
        @Valid @RequestBody request: StudyPostUpdateRequest
    ): ResponseEntity<ApiResponse<StudyPostResponse>> {
        return ApiResponse.success(SuccessCode.POST_UPDATED, updateStudyPostUseCase.execute(userId, postId, request))
    }

    @DeleteMapping("/{postId}")
    override fun deletePost(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable postId: UUID
    ): ResponseEntity<ApiResponse<Void>> {
        deleteStudyPostUseCase.execute(userId, postId)
        return ApiResponse.success(SuccessCode.POST_DELETED)
    }

    @PatchMapping("/{postId}/close")
    override fun closePost(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable postId: UUID
    ): ResponseEntity<ApiResponse<StudyPostResponse>> {
        return ApiResponse.success(SuccessCode.POST_UPDATED, closeStudyPostUseCase.execute(userId, postId))
    }

    @GetMapping("/{postId}/team")
    override fun findTeamByPostId(@PathVariable postId: UUID): ResponseEntity<ApiResponse<StudyTeamResponse>> {
        return ApiResponse.success(SuccessCode.TEAM_FOUND, findStudyTeamUseCase.executeByPostId(postId))
    }

    @GetMapping("/search")
    override fun searchPosts(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) techStack: String?,
        @RequestParam(required = false) status: StudyPostStatus?,
        @RequestParam(required = false, defaultValue = "0") maxMembers: Int,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<StudyPostSummaryResponse>>> {
        return ApiResponse.success(SuccessCode.POST_LIST,
            postSearchService.search(keyword, techStack, status, maxMembers, pageable)
                .map { StudyPostSummaryResponse.from(it) })
    }
}

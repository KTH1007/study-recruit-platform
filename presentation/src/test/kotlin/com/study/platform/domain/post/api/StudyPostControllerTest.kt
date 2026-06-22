package com.study.platform.domain.post.api

import com.study.platform.domain.post.application.PostSearchService
import com.study.platform.domain.post.dto.request.StudyPostCreateRequest
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest
import com.study.platform.domain.post.dto.response.StudyPostResponse
import com.study.platform.domain.post.model.StudyPostStatus
import com.study.platform.domain.post.usecase.CloseStudyPostUseCase
import com.study.platform.domain.post.usecase.CreateStudyPostUseCase
import com.study.platform.domain.post.usecase.DeleteStudyPostUseCase
import com.study.platform.domain.post.usecase.FindStudyPostUseCase
import com.study.platform.domain.post.usecase.FindStudyPostsUseCase
import com.study.platform.domain.post.usecase.UpdateStudyPostUseCase
import com.study.platform.domain.team.usecase.FindStudyTeamUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.idempotency.IdempotencyObjectStoragePort
import com.study.platform.global.idempotency.IdempotencyStoragePort
import com.study.platform.global.jwt.JwtProvider
import com.study.platform.global.ratelimit.RateLimitStoragePort
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any as anyNonNull
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.mockito.BDDMockito.willThrow
import com.study.platform.support.TestSecurityConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.util.UUID

@WebMvcTest(StudyPostController::class)
@Import(TestSecurityConfig::class)
class StudyPostControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var findStudyPostsUseCase: FindStudyPostsUseCase

    @MockitoBean
    private lateinit var findStudyPostUseCase: FindStudyPostUseCase

    @MockitoBean
    private lateinit var createStudyPostUseCase: CreateStudyPostUseCase

    @MockitoBean
    private lateinit var updateStudyPostUseCase: UpdateStudyPostUseCase

    @MockitoBean
    private lateinit var deleteStudyPostUseCase: DeleteStudyPostUseCase

    @MockitoBean
    private lateinit var closeStudyPostUseCase: CloseStudyPostUseCase

    @MockitoBean
    private lateinit var findStudyTeamUseCase: FindStudyTeamUseCase

    @MockitoBean
    private lateinit var postSearchService: PostSearchService

    @MockitoBean
    private lateinit var jwtProvider: JwtProvider

    @MockitoBean
    private lateinit var jpaMetamodelMappingContext: JpaMetamodelMappingContext

    @MockitoBean
    private lateinit var idempotencyObjectStoragePort: IdempotencyObjectStoragePort

    @MockitoBean
    private lateinit var idempotencyStoragePort: IdempotencyStoragePort

    @MockitoBean
    private lateinit var rateLimitStoragePort: RateLimitStoragePort

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var userId: UUID
    private lateinit var postId: UUID
    private lateinit var postResponse: StudyPostResponse
    private lateinit var auth: UsernamePasswordAuthenticationToken

    @BeforeEach
    fun setUp() {
        userId = UUID.randomUUID()
        postId = UUID.randomUUID()
        postResponse = StudyPostResponse(
            postId, userId, "작성자",
            "스터디 모집", "열심히 합니다", "Java", 3,
            LocalDateTime.now().plusDays(7), StudyPostStatus.OPEN,
            LocalDateTime.now(), LocalDateTime.now()
        )
        auth = UsernamePasswordAuthenticationToken(userId, null, listOf())

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId)
        given(rateLimitStoragePort.isAllowed(anyString(), anyLong(), anyLong())).willReturn(true)
    }

    @Test
    fun `findPost_성공`() {
        given(findStudyPostUseCase.execute(anyNonNull())).willReturn(postResponse)

        mockMvc.perform(get("/api/posts/{postId}", postId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value("스터디 모집"))
    }

    @Test
    fun `findPost_없는게시글_404`() {
        given(findStudyPostUseCase.execute(anyNonNull()))
            .willThrow(CustomException(ErrorCode.POST_NOT_FOUND))

        mockMvc.perform(get("/api/posts/{postId}", postId))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `createPost_성공`() {
        val request = StudyPostCreateRequest(
            "스터디 모집", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7)
        )
        given(createStudyPostUseCase.execute(anyNonNull(), anyNonNull())).willReturn(postResponse)

        mockMvc.perform(
            post("/api/posts")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value("스터디 모집"))
    }

    @Test
    fun `createPost_제목빈값_400`() {
        val request = StudyPostCreateRequest(
            "", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7)
        )

        mockMvc.perform(
            post("/api/posts")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `createPost_최대인원1명_400`() {
        val request = StudyPostCreateRequest(
            "스터디 모집", "열심히 합니다", "Java", 1, LocalDateTime.now().plusDays(7)
        )

        mockMvc.perform(
            post("/api/posts")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `updatePost_성공`() {
        val request = StudyPostUpdateRequest(
            "수정된 제목", "수정된 내용", "Kotlin", 5, LocalDateTime.now().plusDays(14)
        )
        val updated = StudyPostResponse(
            postId, userId, "작성자", "수정된 제목", "수정된 내용", "Kotlin", 5,
            LocalDateTime.now().plusDays(14), StudyPostStatus.OPEN,
            LocalDateTime.now(), LocalDateTime.now()
        )
        given(updateStudyPostUseCase.execute(anyNonNull(), anyNonNull(), anyNonNull())).willReturn(updated)

        mockMvc.perform(
            patch("/api/posts/{postId}", postId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.title").value("수정된 제목"))
    }

    @Test
    fun `updatePost_작성자아님_403`() {
        val request = StudyPostUpdateRequest(
            "수정된 제목", "수정된 내용", "Kotlin", 5, LocalDateTime.now().plusDays(14)
        )
        given(updateStudyPostUseCase.execute(anyNonNull(), anyNonNull(), anyNonNull()))
            .willThrow(CustomException(ErrorCode.FORBIDDEN))

        mockMvc.perform(
            patch("/api/posts/{postId}", postId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `deletePost_성공`() {
        willDoNothing().given(deleteStudyPostUseCase).execute(anyNonNull(), anyNonNull())

        mockMvc.perform(
            delete("/api/posts/{postId}", postId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `closePost_성공`() {
        val closed = StudyPostResponse(
            postId, userId, "작성자", "스터디 모집", "열심히 합니다", "Java", 3,
            LocalDateTime.now().plusDays(7), StudyPostStatus.CLOSED,
            LocalDateTime.now(), LocalDateTime.now()
        )
        given(closeStudyPostUseCase.execute(anyNonNull(), anyNonNull())).willReturn(closed)

        mockMvc.perform(
            patch("/api/posts/{postId}/close", postId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.status").value("CLOSED"))
    }
}

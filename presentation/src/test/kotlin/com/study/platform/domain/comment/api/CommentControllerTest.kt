package com.study.platform.domain.comment.api

import com.study.platform.domain.comment.dto.request.CommentCreateRequest
import com.study.platform.domain.comment.dto.request.CommentUpdateRequest
import com.study.platform.domain.comment.dto.response.CommentResponse
import com.study.platform.domain.comment.usecase.CreateCommentUseCase
import com.study.platform.domain.comment.usecase.DeleteCommentUseCase
import com.study.platform.domain.comment.usecase.FindCommentsUseCase
import com.study.platform.domain.comment.usecase.UpdateCommentUseCase
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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

@WebMvcTest(CommentController::class)
@Import(TestSecurityConfig::class)
class CommentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var findCommentsUseCase: FindCommentsUseCase

    @MockitoBean
    private lateinit var createCommentUseCase: CreateCommentUseCase

    @MockitoBean
    private lateinit var updateCommentUseCase: UpdateCommentUseCase

    @MockitoBean
    private lateinit var deleteCommentUseCase: DeleteCommentUseCase

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
    private lateinit var commentId: UUID
    private lateinit var commentResponse: CommentResponse
    private lateinit var auth: UsernamePasswordAuthenticationToken

    @BeforeEach
    fun setUp() {
        userId = UUID.randomUUID()
        postId = UUID.randomUUID()
        commentId = UUID.randomUUID()
        commentResponse = CommentResponse(commentId, userId, "작성자", "좋은 스터디네요", LocalDateTime.now())
        auth = UsernamePasswordAuthenticationToken(userId, null, listOf())

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId)
        given(rateLimitStoragePort.isAllowed(anyString(), anyLong(), anyLong())).willReturn(true)
    }

    @Test
    fun `findComments_성공`() {
        given(findCommentsUseCase.execute(anyNonNull(), anyNonNull()))
            .willReturn(PageImpl(listOf(commentResponse), PageRequest.of(0, 20), 1))

        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].content").value("좋은 스터디네요"))
    }

    @Test
    fun `createComment_성공`() {
        val request = CommentCreateRequest("좋은 스터디네요")
        given(createCommentUseCase.execute(anyNonNull(), anyNonNull(), anyNonNull())).willReturn(commentResponse)

        mockMvc.perform(
            post("/api/posts/{postId}/comments", postId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").value("좋은 스터디네요"))
    }

    @Test
    fun `createComment_내용빈값_400`() {
        val request = CommentCreateRequest("")

        mockMvc.perform(
            post("/api/posts/{postId}/comments", postId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `createComment_내용500자초과_400`() {
        val request = CommentCreateRequest("a".repeat(501))

        mockMvc.perform(
            post("/api/posts/{postId}/comments", postId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `updateComment_성공`() {
        val request = CommentUpdateRequest("수정된 댓글")
        val updated = CommentResponse(commentId, userId, "작성자", "수정된 댓글", LocalDateTime.now())
        given(updateCommentUseCase.execute(anyNonNull(), anyNonNull(), anyNonNull())).willReturn(updated)

        mockMvc.perform(
            patch("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.content").value("수정된 댓글"))
    }

    @Test
    fun `updateComment_작성자아님_403`() {
        val request = CommentUpdateRequest("수정된 댓글")
        given(updateCommentUseCase.execute(anyNonNull(), anyNonNull(), anyNonNull()))
            .willThrow(CustomException(ErrorCode.NOT_COMMENT_AUTHOR))

        mockMvc.perform(
            patch("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `deleteComment_성공`() {
        willDoNothing().given(deleteCommentUseCase).execute(anyNonNull(), anyNonNull())

        mockMvc.perform(
            delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `deleteComment_작성자아님_403`() {
        willThrow(CustomException(ErrorCode.NOT_COMMENT_AUTHOR))
            .given(deleteCommentUseCase).execute(anyNonNull(), anyNonNull())

        mockMvc.perform(
            delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                .with(authentication(auth))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }
}

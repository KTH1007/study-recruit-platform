package com.study.platform.domain.comment.api;
import com.study.platform.global.idempotency.IdempotencyObjectStoragePort;
import com.study.platform.global.idempotency.IdempotencyStoragePort;
import com.study.platform.global.ratelimit.RateLimitStoragePort;

import com.study.platform.domain.comment.dto.request.CommentCreateRequest;
import com.study.platform.domain.comment.dto.request.CommentUpdateRequest;
import com.study.platform.domain.comment.dto.response.CommentResponse;
import com.study.platform.domain.comment.usecase.CreateCommentUseCase;
import com.study.platform.domain.comment.usecase.DeleteCommentUseCase;
import com.study.platform.domain.comment.usecase.FindCommentsUseCase;
import com.study.platform.domain.comment.usecase.UpdateCommentUseCase;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindCommentsUseCase findCommentsUseCase;

    @MockitoBean
    private CreateCommentUseCase createCommentUseCase;

    @MockitoBean
    private UpdateCommentUseCase updateCommentUseCase;

    @MockitoBean
    private DeleteCommentUseCase deleteCommentUseCase;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private IdempotencyObjectStoragePort idempotencyObjectStoragePort;

    @MockitoBean
    private IdempotencyStoragePort idempotencyStoragePort;

    @MockitoBean
    private RateLimitStoragePort rateLimitStoragePort;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID postId;
    private UUID commentId;
    private CommentResponse commentResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        commentResponse = new CommentResponse(commentId, userId, "작성자", "좋은 스터디네요", LocalDateTime.now());

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId);
    }

    @Test
    void findComments_성공() throws Exception {
        // given
        given(findCommentsUseCase.execute(any(), any()))
                .willReturn(new PageImpl<>(List.of(commentResponse), PageRequest.of(0, 20), 1));

        // when & then
        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].content").value("좋은 스터디네요"));
    }

    @Test
    void createComment_성공() throws Exception {
        // given
        CommentCreateRequest request = new CommentCreateRequest("좋은 스터디네요");
        given(createCommentUseCase.execute(any(), any(), any())).willReturn(commentResponse);

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("좋은 스터디네요"));
    }

    @Test
    void createComment_내용빈값_400() throws Exception {
        // given
        CommentCreateRequest request = new CommentCreateRequest("");

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_내용500자초과_400() throws Exception {
        // given
        CommentCreateRequest request = new CommentCreateRequest("a".repeat(501));

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateComment_성공() throws Exception {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");
        CommentResponse updated = new CommentResponse(commentId, userId, "작성자", "수정된 댓글", LocalDateTime.now());
        given(updateCommentUseCase.execute(any(), any(), any())).willReturn(updated);

        // when & then
        mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("수정된 댓글"));
    }

    @Test
    void updateComment_작성자아님_403() throws Exception {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");
        given(updateCommentUseCase.execute(any(), any(), any()))
                .willThrow(new CustomException(ErrorCode.NOT_COMMENT_AUTHOR));

        // when & then
        mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteComment_성공() throws Exception {
        // given
        willDoNothing().given(deleteCommentUseCase).execute(any(), any());

        // when & then
        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteComment_작성자아님_403() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.NOT_COMMENT_AUTHOR))
                .given(deleteCommentUseCase).execute(any(), any());

        // when & then
        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}

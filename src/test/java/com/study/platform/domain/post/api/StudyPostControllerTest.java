package com.study.platform.domain.post.api;

import com.study.platform.domain.post.application.PostSearchService;
import com.study.platform.domain.post.application.StudyPostService;
import com.study.platform.domain.post.dto.request.StudyPostCreateRequest;
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest;
import com.study.platform.domain.post.dto.response.StudyPostResponse;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.domain.team.application.StudyTeamService;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudyPostController.class)
class StudyPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyPostService studyPostService;

    @MockitoBean
    private StudyTeamService studyTeamService;

    @MockitoBean
    private PostSearchService postSearchService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID postId;
    private StudyPostResponse postResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        postResponse = new StudyPostResponse(
                postId, userId, "작성자",
                "스터디 모집", "열심히 합니다", "Java", 3,
                LocalDateTime.now().plusDays(7), StudyPostStatus.OPEN,
                LocalDateTime.now(), LocalDateTime.now()
        );

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId);
    }

    @Test
    void findPost_성공() throws Exception {
        // given
        given(studyPostService.findPost(any())).willReturn(postResponse);

        // when & then
        mockMvc.perform(get("/api/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("스터디 모집"));
    }

    @Test
    void findPost_없는게시글_404() throws Exception {
        // given
        given(studyPostService.findPost(any()))
                .willThrow(new CustomException(ErrorCode.POST_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/posts/{postId}", postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createPost_성공() throws Exception {
        // given
        StudyPostCreateRequest request = new StudyPostCreateRequest(
                "스터디 모집", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7));
        given(studyPostService.createPost(any(), any())).willReturn(postResponse);

        // when & then
        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("스터디 모집"));
    }

    @Test
    void createPost_제목빈값_400() throws Exception {
        // given
        StudyPostCreateRequest request = new StudyPostCreateRequest(
                "", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7));

        // when & then
        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPost_최대인원1명_400() throws Exception {
        // given
        StudyPostCreateRequest request = new StudyPostCreateRequest(
                "스터디 모집", "열심히 합니다", "Java", 1, LocalDateTime.now().plusDays(7));

        // when & then
        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePost_성공() throws Exception {
        // given
        StudyPostUpdateRequest request = new StudyPostUpdateRequest(
                "수정된 제목", "수정된 내용", "Kotlin", 5, LocalDateTime.now().plusDays(14));
        StudyPostResponse updated = new StudyPostResponse(
                postId, userId, "작성자", "수정된 제목", "수정된 내용", "Kotlin", 5,
                LocalDateTime.now().plusDays(14), StudyPostStatus.OPEN,
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(studyPostService.updatePost(any(), any(), any())).willReturn(updated);

        // when & then
        mockMvc.perform(patch("/api/posts/{postId}", postId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 제목"));
    }

    @Test
    void updatePost_작성자아님_403() throws Exception {
        // given
        StudyPostUpdateRequest request = new StudyPostUpdateRequest(
                "수정된 제목", "수정된 내용", "Kotlin", 5, LocalDateTime.now().plusDays(14));
        given(studyPostService.updatePost(any(), any(), any()))
                .willThrow(new CustomException(ErrorCode.FORBIDDEN));

        // when & then
        mockMvc.perform(patch("/api/posts/{postId}", postId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deletePost_성공() throws Exception {
        // given
        willDoNothing().given(studyPostService).deletePost(any(), any());

        // when & then
        mockMvc.perform(delete("/api/posts/{postId}", postId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void closePost_성공() throws Exception {
        // given
        StudyPostResponse closed = new StudyPostResponse(
                postId, userId, "작성자", "스터디 모집", "열심히 합니다", "Java", 3,
                LocalDateTime.now().plusDays(7), StudyPostStatus.CLOSED,
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(studyPostService.closePost(any(), any())).willReturn(closed);

        // when & then
        mockMvc.perform(patch("/api/posts/{postId}/close", postId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }
}

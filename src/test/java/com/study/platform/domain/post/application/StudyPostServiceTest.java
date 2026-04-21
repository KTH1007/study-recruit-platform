package com.study.platform.domain.post.application;

import com.study.platform.domain.post.dto.request.StudyPostCreateRequest;
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest;
import com.study.platform.domain.post.dto.response.StudyPostResponse;
import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class StudyPostServiceTest {

    @Mock
    private StudyPostRepository studyPostRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private StudyPostService studyPostService;

    private UUID authorId;
    private UUID postId;
    private User author;
    private StudyPost post;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        postId = UUID.randomUUID();

        author = User.create("kakao1", "작성자", "author@test.com");
        ReflectionTestUtils.setField(author, "id", authorId);

        post = StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(post, "id", postId);
    }

    @Test
    void findPost_성공() {
        // given
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));

        // when
        StudyPostResponse response = studyPostService.findPost(postId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(postId);
        assertThat(response.title()).isEqualTo("스터디 모집");
    }

    @Test
    void findPost_게시글없음_예외발생() {
        // given
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyPostService.findPost(postId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void createPost_성공() {
        // given
        StudyPostCreateRequest request = new StudyPostCreateRequest(
                "스터디 모집", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7));
        given(userRepository.findById(authorId)).willReturn(Optional.of(author));
        given(studyPostRepository.saveAndFlush(any())).willReturn(post);

        // when
        StudyPostResponse response = studyPostService.createPost(authorId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("스터디 모집");
        then(eventPublisher).should().publishEvent(any(PostSyncEvent.class));
    }

    @Test
    void createPost_사용자없음_예외발생() {
        // given
        StudyPostCreateRequest request = new StudyPostCreateRequest(
                "스터디 모집", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7));
        given(userRepository.findById(authorId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyPostService.createPost(authorId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void updatePost_성공() {
        // given
        StudyPostUpdateRequest request = new StudyPostUpdateRequest(
                "수정된 제목", "수정된 내용", "Kotlin", 5, LocalDateTime.now().plusDays(14));
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));

        // when
        StudyPostResponse response = studyPostService.updatePost(authorId, postId, request);

        // then
        assertThat(response.title()).isEqualTo("수정된 제목");
        then(eventPublisher).should().publishEvent(any(PostSyncEvent.class));
    }

    @Test
    void updatePost_작성자아님_예외발생() {
        // given
        UUID otherId = UUID.randomUUID();
        StudyPostUpdateRequest request = new StudyPostUpdateRequest(
                "수정된 제목", "수정된 내용", "Kotlin", 5, LocalDateTime.now().plusDays(14));
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> studyPostService.updatePost(otherId, postId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    void deletePost_성공() {
        // given
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));

        // when
        studyPostService.deletePost(authorId, postId);

        // then
        then(studyPostRepository).should().delete(post);
        then(eventPublisher).should().publishEvent(any(PostSyncEvent.class));
    }

    @Test
    void deletePost_작성자아님_예외발생() {
        // given
        UUID otherId = UUID.randomUUID();
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> studyPostService.deletePost(otherId, postId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    void closePost_성공() {
        // given
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));

        // when
        StudyPostResponse response = studyPostService.closePost(authorId, postId);

        // then
        assertThat(response.status()).isEqualTo(StudyPostStatus.CLOSED);
        then(eventPublisher).should().publishEvent(any(PostSyncEvent.class));
    }

    @Test
    void closePost_작성자아님_예외발생() {
        // given
        UUID otherId = UUID.randomUUID();
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> studyPostService.closePost(otherId, postId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }
}

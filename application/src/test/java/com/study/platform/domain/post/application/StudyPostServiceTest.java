package com.study.platform.domain.post.application;

import com.study.platform.domain.post.dto.request.StudyPostCreateRequest;
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest;
import com.study.platform.domain.post.dto.response.StudyPostResponse;
import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.outbox.application.OutboxEventService;
import com.study.platform.support.fake.FakeDomainEventPublisher;
import com.study.platform.support.fake.FakeOutboxEventRepository;
import com.study.platform.support.fake.FakeStudyPostQueryPort;
import com.study.platform.support.fake.FakeStudyPostRepository;
import com.study.platform.support.fake.FakeUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudyPostServiceTest {

    private FakeStudyPostRepository studyPostRepository;
    private FakeUserRepository userRepository;
    private FakeDomainEventPublisher eventPublisher;
    private OutboxEventService outboxEventService;

    private FindStudyPostService findStudyPostService;
    private CreateStudyPostService createStudyPostService;
    private UpdateStudyPostService updateStudyPostService;
    private DeleteStudyPostService deleteStudyPostService;
    private CloseStudyPostService closeStudyPostService;

    private UUID authorId;
    private UUID postId;
    private User author;
    private StudyPost post;

    @BeforeEach
    void setUp() {
        studyPostRepository = new FakeStudyPostRepository();
        userRepository = new FakeUserRepository();
        eventPublisher = new FakeDomainEventPublisher();
        outboxEventService = new OutboxEventService(new FakeOutboxEventRepository());
        ObjectMapper objectMapper = new ObjectMapper();

        findStudyPostService = new FindStudyPostService(studyPostRepository);
        createStudyPostService = new CreateStudyPostService(studyPostRepository, userRepository, eventPublisher, outboxEventService, objectMapper);
        updateStudyPostService = new UpdateStudyPostService(studyPostRepository, eventPublisher, outboxEventService, objectMapper);
        deleteStudyPostService = new DeleteStudyPostService(studyPostRepository, eventPublisher, outboxEventService, objectMapper);
        closeStudyPostService = new CloseStudyPostService(studyPostRepository, eventPublisher, outboxEventService, objectMapper);

        authorId = UUID.randomUUID();
        postId = UUID.randomUUID();

        author = User.create("kakao1", "작성자", "author@test.com");
        ReflectionTestUtils.setField(author, "id", authorId);

        post = StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(post, "id", postId);

        userRepository.save(author);
        studyPostRepository.save(post);
    }

    @Test
    void findPost_성공() {
        // when
        StudyPostResponse response = findStudyPostService.execute(postId);

        // then
        assertThat(response.id()).isEqualTo(postId);
        assertThat(response.title()).isEqualTo("스터디 모집");
    }

    @Test
    void findPost_게시글없음_예외발생() {
        // when & then
        assertThatThrownBy(() -> findStudyPostService.execute(UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void createPost_성공() {
        // given
        StudyPostCreateRequest request = new StudyPostCreateRequest(
                "새 스터디", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7));

        // when
        StudyPostResponse response = createStudyPostService.execute(authorId, request);

        // then
        assertThat(response.title()).isEqualTo("새 스터디");
        assertThat(eventPublisher.hasEventOf(PostSyncEvent.class)).isTrue();
    }

    @Test
    void createPost_사용자없음_예외발생() {
        // given
        StudyPostCreateRequest request = new StudyPostCreateRequest(
                "새 스터디", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7));

        // when & then
        assertThatThrownBy(() -> createStudyPostService.execute(UUID.randomUUID(), request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void updatePost_성공() {
        // given
        StudyPostUpdateRequest request = new StudyPostUpdateRequest(
                "수정된 제목", "수정된 내용", "Kotlin", 5, LocalDateTime.now().plusDays(14));

        // when
        StudyPostResponse response = updateStudyPostService.execute(authorId, postId, request);

        // then
        assertThat(response.title()).isEqualTo("수정된 제목");
        assertThat(eventPublisher.hasEventOf(PostSyncEvent.class)).isTrue();
    }

    @Test
    void updatePost_작성자아님_예외발생() {
        // given
        StudyPostUpdateRequest request = new StudyPostUpdateRequest(
                "수정된 제목", "수정된 내용", "Kotlin", 5, LocalDateTime.now().plusDays(14));

        // when & then
        assertThatThrownBy(() -> updateStudyPostService.execute(UUID.randomUUID(), postId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    void deletePost_성공() {
        // when
        deleteStudyPostService.execute(authorId, postId);

        // then
        assertThat(studyPostRepository.findById(postId)).isEmpty();
        assertThat(eventPublisher.hasEventOf(PostSyncEvent.class)).isTrue();
    }

    @Test
    void deletePost_작성자아님_예외발생() {
        // when & then
        assertThatThrownBy(() -> deleteStudyPostService.execute(UUID.randomUUID(), postId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    void closePost_성공() {
        // when
        StudyPostResponse response = closeStudyPostService.execute(authorId, postId);

        // then
        assertThat(response.status()).isEqualTo(StudyPostStatus.CLOSED);
        assertThat(eventPublisher.hasEventOf(PostSyncEvent.class)).isTrue();
    }

    @Test
    void closePost_작성자아님_예외발생() {
        // when & then
        assertThatThrownBy(() -> closeStudyPostService.execute(UUID.randomUUID(), postId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }
}

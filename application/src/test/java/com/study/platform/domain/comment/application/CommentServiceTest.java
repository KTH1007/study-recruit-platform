package com.study.platform.domain.comment.application;

import com.study.platform.domain.comment.dto.request.CommentCreateRequest;
import com.study.platform.domain.comment.dto.request.CommentUpdateRequest;
import com.study.platform.domain.comment.dto.response.CommentResponse;
import com.study.platform.domain.comment.event.CommentCreatedEvent;
import com.study.platform.domain.comment.event.MentionEvent;
import com.study.platform.domain.comment.model.Comment;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.support.fake.FakeCommentQueryPort;
import com.study.platform.support.fake.FakeCommentRepository;
import com.study.platform.support.fake.FakeDomainEventPublisher;
import com.study.platform.support.fake.FakeStudyPostRepository;
import com.study.platform.support.fake.FakeUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentServiceTest {

    private FakeCommentRepository commentRepository;
    private FakeStudyPostRepository studyPostRepository;
    private FakeUserRepository userRepository;
    private FakeDomainEventPublisher eventPublisher;

    private CreateCommentService createCommentService;
    private UpdateCommentService updateCommentService;
    private DeleteCommentService deleteCommentService;

    private UUID authorId;
    private UUID commenterId;
    private UUID postId;
    private User author;
    private User commenter;
    private StudyPost post;

    @BeforeEach
    void setUp() {
        commentRepository = new FakeCommentRepository();
        studyPostRepository = new FakeStudyPostRepository();
        userRepository = new FakeUserRepository();
        eventPublisher = new FakeDomainEventPublisher();

        createCommentService = new CreateCommentService(commentRepository, studyPostRepository, userRepository, eventPublisher);
        updateCommentService = new UpdateCommentService(commentRepository);
        deleteCommentService = new DeleteCommentService(commentRepository);

        authorId = UUID.randomUUID();
        commenterId = UUID.randomUUID();
        postId = UUID.randomUUID();

        author = User.create("kakao1", "작성자", "author@test.com");
        ReflectionTestUtils.setField(author, "id", authorId);

        commenter = User.create("kakao2", "댓글작성자", "commenter@test.com");
        ReflectionTestUtils.setField(commenter, "id", commenterId);

        post = StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(post, "id", postId);

        studyPostRepository.save(post);
        userRepository.save(author);
        userRepository.save(commenter);
    }

    @Test
    void createComment_성공() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("좋은 스터디네요");

        // when
        CommentResponse response = createCommentService.execute(commenterId, postId, request);

        // then
        assertThat(response.content()).isEqualTo("좋은 스터디네요");
        assertThat(eventPublisher.hasEventOf(CommentCreatedEvent.class)).isTrue();
    }

    @Test
    void createComment_게시글없음_예외발생() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("좋은 스터디네요");

        // when & then
        assertThatThrownBy(() -> createCommentService.execute(commenterId, UUID.randomUUID(), request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void createComment_사용자없음_예외발생() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("좋은 스터디네요");

        // when & then
        assertThatThrownBy(() -> createCommentService.execute(UUID.randomUUID(), postId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void createComment_작성자댓글시_방장알림미발행() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("제 글에 댓글 달아요");

        // when
        createCommentService.execute(authorId, postId, request);

        // then
        assertThat(eventPublisher.hasEventOf(CommentCreatedEvent.class)).isFalse();
    }

    @Test
    void createComment_멘션포함시_멘션알림발행() {
        // given
        User mentionedUser = User.create("kakao3", "멘션대상", "mentioned@test.com");
        ReflectionTestUtils.setField(mentionedUser, "id", UUID.randomUUID());
        userRepository.save(mentionedUser);
        CommentCreateRequest request = new CommentCreateRequest("@멘션대상 확인해주세요");

        // when
        createCommentService.execute(commenterId, postId, request);

        // then
        assertThat(eventPublisher.hasEventOf(MentionEvent.class)).isTrue();
    }

    @Test
    void createComment_본인멘션시_멘션알림미발행() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("@댓글작성자 본인 멘션");

        // when
        createCommentService.execute(commenterId, postId, request);

        // then
        assertThat(eventPublisher.hasEventOf(MentionEvent.class)).isFalse();
    }

    @Test
    void updateComment_성공() {
        // given
        Comment comment = Comment.create(post, commenter, "원본 댓글", eventPublisher);
        UUID commentId = UUID.randomUUID();
        ReflectionTestUtils.setField(comment, "id", commentId);
        commentRepository.save(comment);
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

        // when
        CommentResponse response = updateCommentService.execute(commenterId, commentId, request);

        // then
        assertThat(response.content()).isEqualTo("수정된 댓글");
    }

    @Test
    void updateComment_작성자아님_예외발생() {
        // given
        Comment comment = Comment.create(post, commenter, "원본 댓글", eventPublisher);
        UUID commentId = UUID.randomUUID();
        ReflectionTestUtils.setField(comment, "id", commentId);
        commentRepository.save(comment);
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

        // when & then
        assertThatThrownBy(() -> updateCommentService.execute(UUID.randomUUID(), commentId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_AUTHOR);
    }

    @Test
    void updateComment_댓글없음_예외발생() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

        // when & then
        assertThatThrownBy(() -> updateCommentService.execute(commenterId, UUID.randomUUID(), request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    void deleteComment_성공() {
        // given
        Comment comment = Comment.create(post, commenter, "삭제할 댓글", eventPublisher);
        UUID commentId = UUID.randomUUID();
        ReflectionTestUtils.setField(comment, "id", commentId);
        commentRepository.save(comment);

        // when
        deleteCommentService.execute(commenterId, commentId);

        // then
        assertThat(commentRepository.findByIdWithAuthor(commentId)).isEmpty();
    }

    @Test
    void deleteComment_작성자아님_예외발생() {
        // given
        Comment comment = Comment.create(post, commenter, "삭제할 댓글", eventPublisher);
        UUID commentId = UUID.randomUUID();
        ReflectionTestUtils.setField(comment, "id", commentId);
        commentRepository.save(comment);

        // when & then
        assertThatThrownBy(() -> deleteCommentService.execute(UUID.randomUUID(), commentId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_AUTHOR);
    }

    @Test
    void deleteComment_댓글없음_예외발생() {
        // when & then
        assertThatThrownBy(() -> deleteCommentService.execute(commenterId, UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
    }
}
